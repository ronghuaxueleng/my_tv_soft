var rule={
    title:'爱迪影视',
    host:'https://aidi.tv',
    url:'/show/fyclass--------fypage---.html',
    searchUrl:'/vsearch/-------------.html?wd=**&submit=',
    searchable:2,
    quickSearch:0,
    filterable:0,
    headers:{ 'User-Agent':'MOBILE_UA', },
    class_name:'电影&电视剧&综艺&动漫',
    class_url:'dianying&lianxuju&zongyi&dongman',
    tab_exclude:'app专用|VIP线路',
    play_parse:true,
    double:true,
    推荐:'body .vodlist.vodlist_wi;li;a&&title;.vodlist_thumb.lazyload&&data-original;.pic-text&&Text;a&&href',   
    一级:'.vodlist.vodlist_wi&&li;a&&title;.lazyload&&data-original;.pic-text&&Text;a&&href',
    二级:{"title":"h2&&Text;.data:eq(1)&&Text","img":".lazyload&&data-original","desc":";.content_min li:eq(1)&&Text;;.content_min li:eq(2)&&Text;.content_min li:eq(3)&&Text;.data:eq(4)&&Text","content":".context.clearfix&&Text","tabs":".play_source_tab&&a","lists":".content_playlist:eq(#id) li"},
    搜索:'.searchlist_img;a&&title;.vodlist_thumb.lazyload&&data-original;.pic-text&&Text;a&&href',
}