Ext.define('IS.view.portal.Introduction' ,{
	extend: 'Ext.panel.Panel',
	alias: 'widget.p_introduction',
	width: 600,
	border: 0,
	padding: 5,
	html: 'What is the jWebSocket ItemStorage plug-in?<br>\n\
<div class="intro_text">It is a service that allows to define in runtime your server side database object classes(definitions)\n\
and manage such object collections. Each object of a collection is known as "item".\n\
Collections and their items can be shared for read and write operations across the network supporting realtime object\n\
synchronization.\n\
</div>'

});
