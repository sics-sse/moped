/*///////////////////////////////////////////////////////

	Plugin Name: Content Mirror
	Plugin URI: http://klasehnemark.com
	Description:  
	Author: Klas Ehnemark
	Version: 1.0
	Author URI: http://klasehnemark.com
	
	Copyright (C) 2011 Klas Ehnemark (http://klasehnemark.com)


	ADD CONTENT MIRROR TO WORDPRESS EDItOR
	
////////////////////////////////////////////////////////*/


function content_mirror() {
    return "[contentmirror]";
}


(function() {
	tinymce.create('tinymce.plugins.content_mirror', {

		init : function(ed, url) {
			
			var t = this;
			t.editor = ed;
			t.url = url;
			t._createButtons();

			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('...');
			ed.addCommand('content_mirror', function() {
				
				var el = ed.selection.getNode(), post_id, vp = tinymce.DOM.getViewPort();
				if ( el.nodeName == 'IMG' && ed.dom.getAttrib(el, 'class').indexOf('content_mirror') > -1 ) {
					t._open_content_mirror_dialog( el.getAttribute('title') );
				} else {
					t._open_content_mirror_dialog ();
				}
			});
			
			ed.onMouseDown.add ( function(ed, e) {
				if ( e.target.nodeName == 'IMG' && ed.dom.hasClass(e.target, 'content_mirror') )
					ed.plugins.wordpress._showButtons(e.target, 'wp_gallerybtns');
			});

			ed.onBeforeSetContent.add ( function(ed, o) {
				o.content = t._do_content_mirror( o.content, t );
			});

			ed.onPostProcess.add ( function(ed, o) {
				if (o.get)
					o.content = t._get_content_mirror(o.content, t);
			});
			
			ed.addButton('content_mirror', {
				title: 'Content Mirror',
				image: url + '/images/edit.png',
				cmd: 'content_mirror'
			});
		},
		
		_open_content_mirror_dialog : function ( current_shortcode ) {
		
			post_id = tinymce.DOM.get('post_ID').value;
			var ed = this.editor;
			
			ed.windowManager.open({
				file : tinymce.documentBaseURL + 'admin-ajax.php?action=render_content_mirror_admin_form&post_id=' + post_id,
				width : 448,
				height : 440,
				inline : 1
			}, {
				plugin_url : this.url,
				current_shortcode : current_shortcode
			});
		},

		_do_content_mirror : function(co, t) {
			return co.replace(/\[contentmirror([^\]]*)\]/g, function(a,b){
				return '<img src="' + t.url + '/images/t.gif" class="content_mirror mceItem" title="contentmirror'+tinymce.DOM.encode(b)+'" />';
				//return '<div class="wi_thin_content_mirror_holder"><img src="' + t.url + '/images/t.gif" class="wi_thin_content_mirror mceItem" title="contentmirror'+tinymce.DOM.encode(b)+'" /></div>';
			});
		},
		
		_get_content_mirror : function(co) {

			function getAttr(s, n) {
				n = new RegExp(n + '=\"([^\"]+)\"', 'g').exec(s);
				return n ? tinymce.DOM.decode(n[1]) : '';
			};

			return co.replace(/(?:<p[^>]*>)*(<img[^>]+>)(?:<\/p>)*/g, function(a,im) {
				var cls = getAttr(im, 'class');

				if ( cls.indexOf('content_mirror') != -1 )
					return '<p>['+tinymce.trim(getAttr(im, 'title'))+']</p>';

				return a;
			});
		},
	
		_get_attribut : function (s, n) {
			n = new RegExp(n + '=\"([^\"]+)\"', 'g').exec(s);
			return n ? tinymce.DOM.decode(n[1]) : '';
		},

		_createButtons : function() {
			var t = this, ed = tinyMCE.activeEditor, DOM = tinymce.DOM, editButton, dellButton;

			DOM.remove('wp_gallerybtns');

			DOM.add(document.body, 'div', {
				id : 'wp_gallerybtns',
				style : 'display:none;'
			});

			editButton = DOM.add('wp_gallerybtns', 'img', {
				src : t.url+'/images/edit.png',
				id : 'wp_editgallery',
				width : '24',
				height : '24',
				title : 'Edit Content Mirror'
			});
			
			jQuery(editButton).bind('click', function() {
				var ed = tinyMCE.activeEditor;
				ed.windowManager.bookmark = ed.selection.getBookmark('simple');
				ed.execCommand("content_mirror");
			});

			dellButton = DOM.add('wp_gallerybtns', 'img', {
				src : t.url+'/images/delete.png',
				id : 'wp_delgallery',
				width : '24',
				height : '24',
				title : 'Delete Content Mirror'
			});
			
			jQuery(dellButton).bind('click', function() {
				var ed = tinyMCE.activeEditor, el = ed.selection.getNode();

				if ( el.nodeName == 'IMG' && ed.dom.hasClass(el, 'content_mirror') ) {
					ed.dom.remove(el);

					ed.execCommand('mceRepaint');
					return false;
				}
			});
		},

		getInfo : function() {
			return {
				longname : 'Content Mirror Settings',
				author : 'Klas Ehnemark',
				authorurl : 'http://klasehnemark.com',
				infourl : '',
				version : "1.0"
			};
		}
	});

	tinymce.PluginManager.add('content_mirror', tinymce.plugins.content_mirror);
})();





