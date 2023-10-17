package com.github.catvod.utils;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.github.catvod.spider.Init;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileProvider extends ContentProvider {

    private static final String[] COLUMNS = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};

    private static final String META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";

    private static final String TAG_ROOT_PATH = "root-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";
    private static final String TAG_EXTERNAL_MEDIA = "external-media-path";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";

    private static final String DISPLAYNAME_FIELD = "displayName";

    private static final File DEVICE_ROOT = new File("/");

    private static final HashMap<String, PathStrategy> sCache = new HashMap<>();

    private PathStrategy mStrategy;
    private int mResourceId;

    /**
     * The default FileProvider implementation does not need to be initialized. If you want to
     * override this method, you must provide your own subclass of FileProvider.
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * After the FileProvider is instantiated, this method is called to provide the system with
     * information about the provider.
     *
     * @param context A {@link Context} for the current component.
     * @param info    A {@link ProviderInfo} for the new provider.
     */
    @SuppressWarnings("StringSplitter")
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

        // Check our security attributes
        if (info.exported) {
            throw new SecurityException("Provider must not be exported");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }

        String authority = info.authority.split(";")[0];
        synchronized (sCache) {
            sCache.remove(authority);
        }

        mStrategy = getPathStrategy(context, authority, mResourceId);
    }


    public static Uri getUriForFile(Context context, String authority, File file) {
        final PathStrategy strategy = getPathStrategy(context, authority, 0);
        return strategy.getUriForFile(file);
    }


    @SuppressLint("StreamFiles")
    public static Uri getUriForFile(Context context, String authority, File file, String displayName) {
        Uri uri = getUriForFile(context, authority, file);
        return uri.buildUpon().appendQueryParameter(DISPLAYNAME_FIELD, displayName).build();
    }

    /**
     * Use a content URI returned by
     * {@link #getUriForFile(Context, String, File) getUriForFile()} to get information about a file
     * managed by the FileProvider.
     * FileProvider reports the column names defined in {@link OpenableColumns}:
     * <ul>
     * <li>{@link OpenableColumns#DISPLAY_NAME}</li>
     * <li>{@link OpenableColumns#SIZE}</li>
     * </ul>
     * For more information, see
     * {@link ContentProvider#query(Uri, String[], String, String[], String)
     * ContentProvider.query()}.
     *
     * @param uri           A content URI returned by {@link #getUriForFile}.
     * @param projection    The list of columns to put into the {@link Cursor}. If null all columns are
     *                      included.
     * @param selection     Selection criteria to apply. If null then all data that matches the content
     *                      URI is returned.
     * @param selectionArgs An array of {@link String}, containing arguments to bind to
     *                      the <i>selection</i> parameter. The <i>query</i> method scans <i>selection</i> from left to
     *                      right and iterates through <i>selectionArgs</i>, replacing the current "?" character in
     *                      <i>selection</i> with the value at the current position in <i>selectionArgs</i>. The
     *                      values are bound to <i>selection</i> as {@link String} values.
     * @param sortOrder     A {@link String} containing the column name(s) on which to sort
     *                      the resulting {@link Cursor}.
     * @return A {@link Cursor} containing the results of the query.
     */

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // ContentProvider has already checked granted permissions
        final File file = mStrategy.getFileForUri(uri);
        String displayName = uri.getQueryParameter(DISPLAYNAME_FIELD);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = (displayName == null) ? file.getName() : displayName;
            } else if (OpenableColumns.SIZE.equals(col)) {
                cols[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            }
        }

        cols = copyOf(cols, i);
        values = copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    /**
     * Returns the MIME type of a content URI returned by
     * {@link #getUriForFile(Context, String, File) getUriForFile()}.
     *
     * @param uri A content URI returned by
     *            {@link #getUriForFile(Context, String, File) getUriForFile()}.
     * @return If the associated file has an extension, the MIME type associated with that
     * extension; otherwise <code>application/octet-stream</code>.
     */
    @Override
    public String getType(Uri uri) {
        // ContentProvider has already checked granted permissions
        final File file = mStrategy.getFileForUri(uri);

        final int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = file.getName().substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    /**
     * By default, this method throws an {@link UnsupportedOperationException}. You must
     * subclass FileProvider if you want to provide different functionality.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    /**
     * By default, this method throws an {@link UnsupportedOperationException}. You must
     * subclass FileProvider if you want to provide different functionality.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    /**
     * Deletes the file associated with the specified content URI, as
     * returned by {@link #getUriForFile(Context, String, File) getUriForFile()}. Notice that this
     * method does <b>not</b> throw an {@link IOException}; you must check its return value.
     *
     * @param uri           A content URI for a file, as returned by
     *                      {@link #getUriForFile(Context, String, File) getUriForFile()}.
     * @param selection     Ignored. Set to {@code null}.
     * @param selectionArgs Ignored. Set to {@code null}.
     * @return 1 if the delete succeeds; otherwise, 0.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // ContentProvider has already checked granted permissions
        final File file = mStrategy.getFileForUri(uri);
        return file.delete() ? 1 : 0;
    }

    /**
     * By default, FileProvider automatically returns the
     * {@link ParcelFileDescriptor} for a file associated with a <code>content://</code>
     * {@link Uri}. To get the {@link ParcelFileDescriptor}, call
     * {@link ContentResolver#openFileDescriptor(Uri, String)
     * ContentResolver.openFileDescriptor}.
     * <p>
     * To override this method, you must provide your own subclass of FileProvider.
     *
     * @param uri  A content URI associated with a file, as returned by
     *             {@link #getUriForFile(Context, String, File) getUriForFile()}.
     * @param mode Access mode for the file. May be "r" for read-only access, "rw" for read and
     *             write access, or "rwt" for read and write access that truncates any existing file.
     * @return A new {@link ParcelFileDescriptor} with which you can access the file.
     */
    @SuppressLint("UnknownNullness") // b/171012356
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        final File file = mStrategy.getFileForUri(uri);
        final int fileMode = modeToMode(mode);
        return ParcelFileDescriptor.open(file, fileMode);
    }

    /**
     * Return {@link PathStrategy} for given authority, either by parsing or
     * returning from cache.
     */
    private static PathStrategy getPathStrategy(Context context, String authority, int resourceId) {
        PathStrategy strat;
        synchronized (sCache) {
            strat = sCache.get(authority);
            if (strat == null) {
                try {
                    strat = parsePathStrategy(context, authority, resourceId);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                } catch (XmlPullParserException e) {
                    throw new IllegalArgumentException("Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                }
                sCache.put(authority, strat);
            }
        }
        return strat;
    }

    static XmlResourceParser getFileProviderPathsMetaData(Context context, String authority, ProviderInfo info, int resourceId) {
        if (info == null) {
            throw new IllegalArgumentException("Couldn't find meta-data for provider with authority " + authority);
        }

        if (info.metaData == null && resourceId != 0) {
            info.metaData = new Bundle(1);
            info.metaData.putInt(META_DATA_FILE_PROVIDER_PATHS, resourceId);
        }

        final XmlResourceParser in = info.loadXmlMetaData(context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS);
        if (in == null) {
            throw new IllegalArgumentException("Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
        }

        return in;
    }

    /**
     * Parse and return {@link PathStrategy} for given authority as defined in
     * {@link #META_DATA_FILE_PROVIDER_PATHS} {@code <meta-data>}.
     *
     * @see #getPathStrategy(Context, String, int)
     */
    private static PathStrategy parsePathStrategy(Context context, String authority, int resourceId) throws IOException, XmlPullParserException {
        final SimplePathStrategy strat = new SimplePathStrategy(authority);

        final ProviderInfo info = context.getPackageManager().resolveContentProvider(authority, PackageManager.GET_META_DATA);
        final XmlResourceParser in = getFileProviderPathsMetaData(context, authority, info, resourceId);

        int type;
        while ((type = in.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                final String tag = in.getName();

                final String name = in.getAttributeValue(null, ATTR_NAME);
                String path = in.getAttributeValue(null, ATTR_PATH);

                File target = null;
                if (TAG_ROOT_PATH.equals(tag)) {
                    target = DEVICE_ROOT;
                } else if (TAG_FILES_PATH.equals(tag)) {
                    target = context.getFilesDir();
                } else if (TAG_CACHE_PATH.equals(tag)) {
                    target = context.getCacheDir();
                } else if (TAG_EXTERNAL.equals(tag)) {
                    target = Environment.getExternalStorageDirectory();
                } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                    File[] externalCacheDirs = new File[0];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        externalCacheDirs = Init.context().getExternalCacheDirs();
                    }
                    if (externalCacheDirs.length > 0) {
                        target = externalCacheDirs[0];
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && TAG_EXTERNAL_MEDIA.equals(tag)) {
                    File[] externalMediaDirs = Api21Impl.getExternalMediaDirs(context);
                    if (externalMediaDirs.length > 0) {
                        target = externalMediaDirs[0];
                    }
                }

                if (target != null) {
                    strat.addRoot(name, buildPath(target, path));
                }
            }
        }

        return strat;
    }

    /**
     * Strategy for mapping between {@link File} and {@link Uri}.
     * <p>
     * Strategies must be symmetric so that mapping a {@link File} to a
     * {@link Uri} and then back to a {@link File} points at the original
     * target.
     * <p>
     * Strategies must remain consistent across app launches, and not rely on
     * dynamic state. This ensures that any generated {@link Uri} can still be
     * resolved if your process is killed and later restarted.
     *
     * @see SimplePathStrategy
     */
    interface PathStrategy {
        /**
         * Return a {@link Uri} that represents the given {@link File}.
         */
        Uri getUriForFile(File file);

        /**
         * Return a {@link File} that represents the given {@link Uri}.
         */
        File getFileForUri(Uri uri);
    }

    /**
     * Strategy that provides access to files living under a narrow allowed list
     * of filesystem roots. It will throw {@link SecurityException} if callers try
     * accessing files outside the configured roots.
     * <p>
     * For example, if configured with
     * {@code addRoot("myfiles", context.getFilesDir())}, then
     * {@code context.getFileStreamPath("foo.txt")} would map to
     * {@code content://myauthority/myfiles/foo.txt}.
     */
    static class SimplePathStrategy implements PathStrategy {
        private final String mAuthority;
        private final HashMap<String, File> mRoots = new HashMap<>();

        SimplePathStrategy(String authority) {
            mAuthority = authority;
        }

        /**
         * Add a mapping from a name to a filesystem root. The provider only offers
         * access to files that live under configured roots.
         */
        void addRoot(String name, File root) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Name must not be empty");
            }

            try {
                // Resolve to canonical path to keep path checking fast
                root = root.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + root, e);
            }

            mRoots.put(name, root);
        }

        @Override
        public Uri getUriForFile(File file) {
            String path;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            // Find the most-specific root path
            Map.Entry<String, File> mostSpecific = null;
            for (Map.Entry<String, File> root : mRoots.entrySet()) {
                final String rootPath = root.getValue().getPath();
                if (path.startsWith(rootPath) && (mostSpecific == null || rootPath.length() > mostSpecific.getValue().getPath().length())) {
                    mostSpecific = root;
                }
            }

            if (mostSpecific == null) {
                throw new IllegalArgumentException("Failed to find configured root that contains " + path);
            }

            // Start at first char of path under root
            final String rootPath = mostSpecific.getValue().getPath();
            if (rootPath.endsWith("/")) {
                path = path.substring(rootPath.length());
            } else {
                path = path.substring(rootPath.length() + 1);
            }

            // Encode the tag and path separately
            path = Uri.encode(mostSpecific.getKey()) + '/' + Uri.encode(path, "/");
            return new Uri.Builder().scheme("content").authority(mAuthority).encodedPath(path).build();
        }

        @Override
        public File getFileForUri(Uri uri) {
            String path = uri.getEncodedPath();

            final int splitIndex = path.indexOf('/', 1);
            final String tag = Uri.decode(path.substring(1, splitIndex));
            path = Uri.decode(path.substring(splitIndex + 1));

            final File root = mRoots.get(tag);
            if (root == null) {
                throw new IllegalArgumentException("Unable to find configured root for " + uri);
            }

            File file = new File(root, path);
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            if (!file.getPath().startsWith(root.getPath())) {
                throw new SecurityException("Resolved path jumped beyond configured root");
            }

            return file;
        }
    }

    /**
     * Copied from ContentResolver.java
     */
    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    static class Api21Impl {
        private Api21Impl() {
            // This class is not instantiable.
        }

        static File[] getExternalMediaDirs(Context context) {
            // Deprecated, otherwise this would belong on ContextCompat as a public method.
            return context.getExternalMediaDirs();
        }
    }
}
