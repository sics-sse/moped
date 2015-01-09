/*!
 * jQuery HTML Ajax Uploader 2.2
 * Alban Xhaferllari
 * albanx@gmail.com
 * copyright
 * 
 */

(function($)
{
	//Test if support pure ajax upload and create browse file input
	var axtest = document.createElement('input');
	axtest.type = 'file';
	var isAjaxUpload=('multiple' in axtest &&  typeof File != "undefined" &&  typeof (new XMLHttpRequest()).upload != "undefined" );
	axtest = null;
	//isAjaxUpload=false;
	
	/**
	 * @settings object setting
	 * @filename
	 * @filesize size of file in html 5 browser
	 */
    function getURL(settings, fileName, size)
    {
		var getpath	= (typeof(settings.remotePath)=='function')?settings.remotePath():settings.remotePath;
		var params	= [];
		params.push('ax-file-path=' + encodeURIComponent(getpath));
		params.push('ax-allow-ext=' + encodeURIComponent(settings.allowExt.join('|')));
		params.push('ax-file-name=' + encodeURIComponent(fileName));
		params.push('ax-thumbHeight=' + settings.thumbHeight);
		params.push('ax-thumbWidth=' + settings.thumbWidth);
		params.push('ax-thumbPostfix=' + encodeURIComponent(settings.thumbPostfix));
		params.push('ax-thumbPath=' + encodeURIComponent(settings.thumbPath));
		params.push('ax-thumbFormat=' + encodeURIComponent(settings.thumbFormat));
		params.push('ax-maxFileSize=' + encodeURIComponent(settings.maxFileSize));
		params.push('ax-fileSize=' + size);
		
		var otherdata	= (typeof(settings.data)=='function')?settings.data():settings.data;
		if(typeof(otherdata)=='object')
		{
			for(var i in otherdata)
			{
				params.push(i + '=' + encodeURIComponent(otherdata[i]));
			}
		}
		else if(typeof(otherdata)=='string' && otherdata!='')
		{
			params.push(otherdata);
		}
		
		var c	= (settings.url.indexOf('?')==-1)?'?':'&';
		
		return settings.url+c+params.join('&');		
    }
    
    /*
     * Upload ajax function supported by modern browsers Firefox 3.6+, Chrome, Safari
     * IE 10 maybe will support this
     * 
     */
    function uploadAjax(queued, settings)
    {
    	var file		= queued.file;
    	var startByte	= queued.byte;
    	var name		= queued.name;
    	var size		= file.size;
    	var chunkSize	= settings.chunkSize;	//chunk size
		var endByte		= chunkSize + startByte;
		var isLast		= (size - endByte <= 0);
    	var chunk		= file;
    	var xhr 		= new XMLHttpRequest();//prepare xhr for upload
    	var chunkNum	= endByte / chunkSize;
    	queued.xhr		= xhr;
    	
    	if(startByte == 0)	settings.SLOTS++;
    	
    	if(chunkSize == 0)//no divide
    	{
    		chunk	= file;
    		isLast	= true;
    	}
    	else if(file.mozSlice) // moz slice
    	{
    		chunk	= file.mozSlice(startByte, endByte);
    	}
    	else if(file.webkitSlice) //webkit slice
    	{
    		chunk	= file.webkitSlice(startByte, endByte);
    	}
    	else if(file.slice) // w3c slice
    	{
    		chunk	= file.slice(startByte, chunkSize);
    	}
    	else
    	{
    		chunk	= file;
    		isLast	= true;
    	}
    	
    	//abort event, (nothing to do for the moment)
    	xhr.upload.addEventListener('abort', function(e){
    		settings.SLOTS--;
    	}, false); 
    	
    	//progress function, with ajax upload progress can be monitored
    	xhr.upload.addEventListener('progress', function(e)
		{
			if (e.lengthComputable) 
			{
				var perc = Math.round((e.loaded + chunkNum * chunkSize - chunkSize) * 100 / size);
				queued.progress(perc);
			}  
		}, false); 
    	    	
    	xhr.upload.addEventListener('error', function(e){
    		queued.error(this.responseText, name);
    	}, false);  
    	
		xhr.onreadystatechange=function()
		{
			if(this.readyState == 4 && this.status == 200)
			{
				try
				{
					var ret	= JSON.parse(this.responseText);
					if(startByte == 0)
						queued.name	= ret.name;
					
					if(isLast)
					{
						settings.SLOTS--;
						queued.end(ret.name, ret.size, ret.status, ret.info);
					}
					else if(ret.status == 'error')
					{
						throw ret.info;
					}
					else
					{
						queued.byte = endByte;
						uploadAjax(queued, settings);
					}
				}
				catch(err)
				{
					settings.SLOTS--;
					queued.error('error', err);
				}
			}
		};
				
		var finalUrl=getURL(settings, name, size);
		xhr.open('POST', finalUrl + '&ax-start-byte=' + startByte + '&isLast=' + isLast, settings.async);
		xhr.setRequestHeader('Cache-Control', 'no-cache');
		xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');//header
		xhr.setRequestHeader('Content-Type', 'application/octet-stream');//generic stream header
		//TODO set boundary for future version, for sending other data in post rather than only files
		//TODO use sendasbinary or sendasbinarystring
		xhr.send(chunk);

    }
    
    function generateBoundary() {
        return "AJAX-----------------------" + (new Date).getTime();
    }
    
    
    //TODO add fine zoom on light box
    function createPreview(prevC, fileObj, ext, settings)
    {
   		if (fileObj.type.match(/image.*/) && (ext=='jpg' || ext=='gif' || ext=='png') && typeof (FileReader) !== "undefined")
	    {
   			var img = prevC.css('background','none').children('img:first');
		    var reader = new FileReader();  
		    reader.onload =function(e) {
		    	img.css('cursor','pointer').attr('src',e.target.result).click(function(){
		   			var imgloader=new Image();
		   			imgloader.onload = function()
		   			{			 
		   			    var ratio = Math.min($(window).width() / this.width, ($(window).height()-100) / this.height);
		   			    var newWidth = (ratio<1)?this.width * ratio:this.width;
		   			    var newHeight = (ratio<1)?this.height * ratio:this.height;

			    		$('#ax-box').html('<img width="'+newWidth+'" height="'+newHeight+'" src="'+e.target.result+'" /><a>Close</a><span>'+fileObj.name+'</span>')
			    				    .fadeIn(100)
			    				    .css({
			    				    	'top': ($(window).scrollTop()-20+($(window).height()-newHeight)/2) + 'px',
			    				    	'height':(newHeight+20)+'px',
			    				    	'width':newWidth+'px',
			    				    	'left':($(window).width()-newWidth)/2
			    				    });			    		
			    		$('#ax-box').find('a').css('cursor','pointer').click(function(e){
			    			e.preventDefault();
			    			$('#ax-box-shadow, #ax-box').fadeOut(100);
			    		});
			    		
			    		$('#ax-box-shadow').css('height',$(document).height()).fadeIn(100);
		   			};
		   			imgloader.src=e.target.result;
		    	});
		    };  
		    reader.readAsDataURL(fileObj); 
	    }
	    else
	    {
	    	prevC.children('img:first').remove();//FIXME
	    	prevC.addClass('ax-filetype-'+ext);
	    }
    }
    
    function uploadAll(FILES)
    {
    	setTimeout(function(){
    		var stop = true;
    		for(var i = 0;i < FILES.length;i++)
    		{
    			if(FILES[i].xhr === null && FILES[i].status != 'uploaded' && FILES[i].status != 'error')
    			{
    				stop = false;
    				FILES[i].sns();
    			}
    		}
    		if(!stop)	uploadAll(FILES);
    	},300);
    }
        
    function sizeFormat(format, size)
    {
		switch(format)
		{
			case 'gb' : size = size / (1024*1024*1024);break;
			case 'mb' : size = size / (1024*1024);break;
			case 'kb' : size = size / (1024);break;
		}
		return (Math.round(size*100)/100)+' '+format;
    }
    

    function fileTemplate(list, fileobj, settings)
    {
    	var fn		= fileobj.name;
		var size	= sizeFormat(settings.showSize, fileobj.size);
    	var li 		= $('<li />').appendTo(list).attr('title',fn);
    	var prevC	= $('<a class="ax-prev-container" />').appendTo(li);
    	var prevImg	= $('<img class="ax-preview" src="" alt="Preview" />').appendTo(prevC);
    	var details	= $('<div class="ax-details" />').appendTo(li);
    	
	    var nameC	= $('<div class="ax-file-name">'+fn+'</div>').appendTo(details);
	    
	    
	    //if edit file name allowed bind events TODO control conditions inside events?
	    if(settings.editFilename)
	    {
		    nameC.dblclick(function(e){
		    	e.stopPropagation();
		    	var fn_noext = fn.substr(0,fn.lastIndexOf("."));
		    	$(this).html('<input type="text" value="'+fn_noext+'" />.'+fileobj.ext);
		    }).bind('blur',function(e){
	    		e.stopPropagation();
	    		var new_fn = $(this).children('input').val();
	    		if(typeof(new_fn) != 'undefined')
	    		{
	    			var cleanString = new_fn.replace(/[|&;$%@"<>()+,]/g, '');//remove bad filename chars
	    			var final_fn = cleanString+'.'+fileobj.ext;
	    			$(this).html(final_fn);
	    			fileobj.name = final_fn;
	    			if(!isAjaxUpload && fileobj.xhr)//on form upload also rename input hidden input
	    			{
	    				fileobj.xhr.children('input[name="ax-file-name"]').val(final_fn);
	    			}
	    		}
		    });
	    }
	    
	    
	    var sizeC	= $('<div class="ax-file-size">'+size+'</div>').appendTo(details);
    	var progres	= $('<div class="ax-progress" />').appendTo(li);
	    var pBar	= $('<div class="ax-progress-bar" />').appendTo(progres);
	    var pNum	= $('<div class="ax-progress-info">0%</div>').appendTo(progres);
    	var toolbar	= $('<div class="ax-toolbar" />').appendTo(li);   
    	
	    var upload	= $('<a title="Start upload" class="ax-upload ax-button" />').click(function(){
	    	if(settings.enable) fileobj.sns();   
	    }).appendTo(toolbar).append('<span class="ax-upload-icon ax-icon"></span>');
	    
	    var remove	= $('<a title="Remove file" class="ax-remove ax-button" />').click(function(){
	    	if(settings.enable) fileobj.remove();
	    }).appendTo(toolbar).append('<span class="ax-clear-icon ax-icon"></span>');
	    
	    createPreview(prevC, fileobj.file, fileobj.ext, settings);
	    return {pBar:pBar, pNum:pNum, upload:upload, remove:remove, nameC:nameC, li:li, sizeC:sizeC};
    }
    
    //update the file list
    function ajaxList(list, obj, settings)
    {
    	var FILES	= settings.FILES;
    	var tools	= fileTemplate(list, obj, settings);	  
    	var upload	= tools.upload;
    	var remove	= tools.remove;
    	
    	obj.tools=tools;
    	obj.end = function(name, size, status, info)
	    {
	    	settings.UPLOADED++;
	    	this.name	= name;
	    	this.status	= status;
	    	this.info	= info;
	    	this.xhr	= null;
	    	this.byte	= 0;
	    	this.tools.nameC.html(name);
	    	this.tools.li.attr('title', name);
	    	
	    	this.tools.pNum.html('100%');
	    	this.tools.pBar.css('width','100%');
	    	this.tools.upload.removeClass('ax-abort');
	    	
	    	settings.success(name);
	    	if(FILES.length == settings.UPLOADED)
	    	{
	    		var arrFiles = [];
	    		settings.UPLOADED	 = 0;
	    		for(var j = 0; j < FILES.length; j++)	arrFiles.push(FILES[j].name);
	    		settings.finish(arrFiles);
	    		settings.internalFinish(arrFiles);
	    	}
	    };
	    
	    obj.progress = function(p)
	    {
	    	this.tools.pNum.html(p+'%');
	    	this.tools.pBar.css('width',p+'%');
	    };
	    
	    obj.error = function(status, err)
	    {
	    	this.xhr	= null;
	    	this.byte	= 0;
	    	this.status	= status;
	    	this.info	= err;
	    	this.tools.pNum.html(err);
	    	this.tools.pBar.css('width','0%');
	    	this.tools.upload.removeClass('ax-abort');
			settings.error(err, this.name);
	    };
	    //startNstop
	    obj.sns = function(all)
	    {
	    	if(this.xhr !== null)
	    	{
	    		this.xhr.abort();
	    		this.xhr 	= null;
	    		this.byte	= 0;
	    		this.tools.upload.removeClass('ax-abort');
	    	}
	    	else if(settings.SLOTS < settings.maxConnections)
	    	{
	    		this.tools.pNum.html('0%');
	    		this.tools.pBar.css('width','0%');
	    		this.tools.upload.addClass('ax-abort');
	    		uploadAjax(this, settings);
	    	}
	    };
	    
	    obj.remove = function()
	    {
	    	FILES.splice(this.pos, 1);
	    	if(this.xhr)	this.xhr.abort();
	    	this.file		= null;
	    	this.xhr		= null;
	    	for(var j=0; j<FILES.length; j++) FILES[j].pos=j;
	    	this.tools.li.remove();
	    };
    }
    
    function formList(list, obj, settings)
    {
    	var FILES	= settings.FILES;
    	var tools	= fileTemplate(list, obj, settings);
    	var upload	= tools.upload;
    	var remove	= tools.remove;
    	var iframe	= document.getElementById('ax-main-frame');
    	
    	var url		= getURL(settings,'',0);
    	var form	= $('<form action="'+url+'" method="post" target="ax-main-frame" encType="multipart/form-data" />').hide().appendTo(tools.li);
    	form.append('<input type="hidden" value="'+encodeURIComponent(obj.name)+'" name="ax-file-name" />');//input for re-name of file
    	$(obj.file).appendTo(form);
    	obj.xhr = form;
    	
    	obj.tools=tools;
    	obj.end = function(name, size, status, info)
	    {
	    	settings.UPLOADED++;
	    	this.name	= name;
	    	this.status	= status;
	    	this.info	= info;
	    	//this.xhr	= null;
	    	this.byte	= 0;
	    	this.size	= size;
	    	this.tools.nameC.html(name);
	    	this.tools.sizeC.html(sizeFormat(settings.showSize, size));
	    	this.tools.li.attr('title', name);
	    	
	    	if(status == 'error')
	    	{
	    		this.tools.pNum.html(info);
	    		this.tools.pBar.css('width','0%');
	    	}
	    	else
	    	{
	    		this.tools.pNum.html('100%');
	    		this.tools.pBar.css('width','100%');
	    	}
	    	
	    	this.tools.upload.removeClass('ax-abort');
	    	settings.success(name);
	    	if(FILES.length == settings.UPLOADED)
	    	{
	    		var arrFiles 	= [];
	    		settings.UPLOADED		= 0;
	    		for(var j = 0; j < FILES.length; j++)	arrFiles.push(FILES[j].name);
	    		settings.finish(arrFiles);
	    		settings.internalFinish(arrFiles);
	    	}
	    };
	    
	    obj.progress = function(p)
	    {
	    	this.tools.pNum.html(p+'%');
	    	this.tools.pBar.css('width',p+'%');
	    };
	        
	    obj.sns = function()
	    {
	    	if(upload.hasClass('ax-abort'))
	    	{
		    	try{iframe.contentWindow.document.execCommand('Stop');}catch(ex){iframe.contentWindow.stop();}
	    		upload.removeClass('ax-abort');
	    	}
	    	else
	    	{
	    		this.tools.pNum.html(0+'%');
	    		this.tools.pBar.css('width',0+'%');
	    		this.tools.upload.addClass('ax-abort');
	    		uploadForm(this, false, FILES);
	    		
	    	}
	    };
	    
	    obj.remove = function()
	    {
	    	FILES.splice(this.pos, 1);
	    	try{iframe.contentWindow.document.execCommand('Stop');}catch(ex){iframe.contentWindow.stop();}
	    	$(this.file).remove();
	    	this.file		= null;
	    	for(var j=0; j<FILES.length; j++) FILES[j].pos=j;
	    	this.tools.li.remove();
	    };
    }
    
    function uploadForm(obj, all, FILES)
    {   	
    	if(FILES.length>0)
    	{
    		$('#ax-main-frame').unbind('load').bind('load',function(){
    			var frameDoc;
    			if ( this.contentDocument ) 
    			{ // FF
    				frameDoc = this.contentDocument;
    			}
    			else if ( this.contentWindow ) 
    			{ // IE
    				frameDoc = this.contentWindow.document;
    			}
	    		
	    		var ret	= $.parseJSON(frameDoc.body.innerHTML);			
	    		obj.progress(100);
	    		obj.end(ret.name, ret.size, ret.status, ret.info);
	    		
	    		if(all && FILES[obj.pos+1])
	    		{
	    			uploadForm(FILES[obj.pos+1], true, FILES);
	    		}
    		});
	    	obj.xhr.submit();
    	}
    }
    
    function addFiles(arr, fileList, settings)
    {
    	var FILES = settings.FILES;
		for (var i = 0; i < arr.length; i++) 
		{
			var ext, name, size, pos = FILES.length;
			if(isAjaxUpload)
			{
				name	= arr[i].name;
				size	= arr[i].size;
			}
			else
			{
				name	= arr[i].value.replace(/^.*\\/, '');
				size	= 0;
			}
			ext	= name.split('.').pop().toLowerCase();
						
			if(FILES.length < settings.maxFiles && ($.inArray(ext, settings.allowExt)>=0 || settings.allowExt.length==0))
			{
				var obj={pos	: pos,
						byte	: 0,
						sns		: function(){},
						error	: function(){}, 
						end		: function(){}, 
						progress: function(){}, 
						file	: arr[i], 
						status	: 'ok', 
						name	: name,
						size	: size,
						xhr		: null, 
						info	: '',
						ext		: ext};
				FILES.push(obj);
				if(isAjaxUpload)
					ajaxList(fileList, obj, settings);
				else
					formList(fileList, obj, settings);
			}
		}
    }
    

    function startUpload(FILES)
    {
    	(isAjaxUpload)?	uploadAll(FILES) : uploadForm(FILES[0], true, FILES);
    }
    
    function clearQueue(settings)
    {
    	if(!settings.enable) return;
    	while(settings.FILES.length>0)	settings.FILES[0].remove();
    }
    
    var globalSettings = 
    {
    	remotePath : 	'js/',						//remote upload path, can be set also in the php upload script
    	url:			'upload.php',				//php/asp/jsp upload script
    	data:			'',							//other user data to send in GET to the php script
    	async:			true,						//set asyncron upload or not
    	maxFiles:		9999,						//max number of files can be selected
    	allowExt:		[],							//array of allowed upload extesion, can be set also in php script
    	showSize:		'Mb',						//show size in Mb, Kb or bytes, or Gb
    	success:		function(fileName){},		//function that triggers every time a file is uploaded
    	finish:			function(arrFiles){},		//function that triggers when all files are uploaded
    	error:			function(txt,fileName){},	//function that triggers if an error occuors during upload
    	enable:			true,						//start plugin enable or disabled
    	chunkSize:		1024*1024,//default 1Mb,	//if supported send file to server by chunks, not at once
    	maxConnections:	3,							//max parallel connection on multiupload recomended 3, firefox support 6, only for browsers that support file api
    	dropColor:		'red',						//back color of drag & drop area, hex or rgb
    	dropArea:		'self',						//set the id or element of area where to drop files. default self
    	autoStart:		false,						//if true upload will start immediately after drop of files or select of files
    	thumbHeight:	0,							//max thumbnial height if set generate thumbnial of images on server side
    	thumbWidth:		0,							//max thumbnial width if set generate thumbnial of images on server side
    	thumbPostfix:	'_thumb',					//set the post fix of generated thumbs, default filename_thumb.ext,
    	thumbPath:		'',							//set the path where thumbs should be saved, if empty path setted as remotePath
    	thumbFormat:	'',							//default same as image, set thumb output format, jpg, png, gif
    	maxFileSize:	'1001M',					//max file size of single file,
    	form:			null,						//integration with some form, set the form selector or object, and upload will start on form submit
    	editFilename:	false,						//if true allow edit file names before upload, by dblclick
    	sortable:		false						//set if need to sort file list, need jquery-ui
    };
    
	var methods =
	{
		init : function(options)
		{
    	    return this.each(function() 
    	    {	
				var $this = $(this);
				if($this.hasClass('ax-uploader'))//for avoiding two times call errors
				{
					return;
				}
				$this.addClass('ax-uploader').data('author','http://www.albanx.com/');
				
				var settings		= $.extend({},globalSettings,options);
				settings.FILES		= []; //the queue
				settings.UPLOADED	= 0;  //count uploaded files
				settings.SLOTS		= 0;  //slot number
				settings.internalFinish = function(){};
				
				
				//If used with form combination, the bind upload on form submit
				var form = null;
				if($(settings.form).length>0)
					form = $(settings.form);
				else if(settings.form=='parent')
					form = $this.parents('form:first');
				
				if(typeof(form) != 'undefined' && form!=null)
				{
					//on submit form, if there are files, first upload and the submit form
					$(form).bind('submit.ax',function(){
						if(settings.FILES.length>0)
						{
							startUpload(settings.FILES);
							return false;
						}
					});
					
					//this function is run after files are upload, adds hidden inputs with file uploaded paths, and submits the final form
					settings.internalFinish = function(files){
						if(form!=null)
						{
							var basepath = (typeof(settings.remotePath)=='function')?settings.remotePath():settings.remotePath;
							for(var i=0;i<files.length;i++)
							{
								var filepath = basepath+files[i];
								$(form).append('<input name="ax-uploaded-files[]" type="hidden" value="'+filepath+'" />');
							}
							$(form).unbind('submit.ax').submit();
						}
					};
				}
				
				//normalize strings
				settings.showSize	= settings.showSize.toLowerCase();
				settings.allowExt 	= $.map(settings.allowExt, function(n, i){ return n.toLowerCase();  });

				$this.data('settings', settings);
				
				if($('#ax-box').length==0)			$('<div id="ax-box"/>').appendTo('body');
				if($('#ax-box-shadow').length==0)	$('<div id="ax-box-shadow"/>').appendTo('body');
				if($('#ax-main-frame').length==0) 	$('<iframe name="ax-main-frame" id="ax-main-frame" />').hide().appendTo('body');	
				
				//var mainForm  	= $('<form target="ax-main-frame" method="POST" action="" encType="multipart/form-data" />').appendTo($this);
				var fieldSet  	= $('<fieldset />').append('<legend class="ax-legend">Select files</legend>').appendTo($this);
				var browse_c  	= $('<a class="ax-browse-c ax-button" title="Add Files" />').appendTo(fieldSet);//Browse container
				browse_c.append('<span class="ax-plus-icon ax-icon"></span> <span>Add Files</span>');
				
				var browseFiles = $('<input type="file" class="ax-browse" name="ax-files[]" />').attr('multiple',isAjaxUpload).appendTo(browse_c);
				
			    var uploadFiles = $('<a class="ax-upload-all ax-button" title="Upload all files" />').appendTo(fieldSet);
			    uploadFiles.append('<span class="ax-upload-icon ax-icon"></span> <span>Start upload</span>');
			    
			    var removeFiles = $('<a class="ax-clear ax-button" title="Clear all" />').appendTo(fieldSet);
			    removeFiles.append('<span class="ax-clear-icon ax-icon"></span> <span>Remove all</span>');
			    
			    var fileList	= $('<ul class="ax-file-list" />').appendTo(fieldSet);
			    var otherinfo	= $('<span class="ax-net-info"></span>').appendTo($this);
			    
			    //get selected files
			    browseFiles.bind('change',function(){
			    	//disable option			
			    	if(!settings.enable) return;
			    	if(isAjaxUpload)
			    	{
			    		addFiles(this.files, fileList, settings);
			    		if(navigator.userAgent.toLowerCase().indexOf('chrome') > -1);
			    			refreshBrowse();//Chrome "bug??" fix reselecting the same file
			    	}
			    	else
			    	{
			    		addFiles([this], fileList, settings);
			    		$(this).clone(true).val('').appendTo(browse_c);
			    		$(this).hide();
			    	}
			    	
			    	if(settings.autoStart)
			    	{
			    		startUpload(settings.FILES);
			    	}
				});
			    
			    function refreshBrowse(){
			    	var newB=browseFiles.clone(true).appendTo(browse_c);
			    	browseFiles.remove();
			    	browseFiles=newB;
			    }
			    
			    //upload files
			    uploadFiles.bind('click',function(){
			    	if(!settings.enable) return;
			    	
			    	startUpload(settings.FILES);
			    	return false;
			    });
			    
			    //remove all files from list
			    removeFiles.bind('click',function(){
			    	clearQueue(settings);
			    	return false;
			    });    
			    
			    if(isAjaxUpload)
			    {
			    	var dropArea = (settings.dropArea=='self')? this: $(settings.dropArea).get(0);
				    //Add files by drag and drop
			    	dropArea.addEventListener('dragenter',function(e){
			    		e.stopPropagation();  
			    		e.preventDefault(); 
			    	},false);
			    	
			    	dropArea.addEventListener('dragover',function(e){
			    		if(!settings.enable) return;
			    		
			    		e.stopPropagation();  
			    		e.preventDefault(); 
			    		$(this).css('background-color',settings.dropColor); 
			    	},false);
			    	
			    	dropArea.addEventListener('dragleave',function(e){
			    		e.stopPropagation();  
			    		e.preventDefault(); 
			    		$(this).css('background-color',''); 
			    	},false);
			    	dropArea.addEventListener('drop',function(e)
				    {
			    		if(!settings.enable) return;
				    	e.stopPropagation();  
				    	e.preventDefault();
	    				
	    				addFiles(e.dataTransfer.files, fileList, settings);
	
	    				$(this).css('background-color','');
	    				
				    	if(settings.autoStart)
				    	{
				    		startUpload(settings.FILES);
				    	}
	    			},false);	
			    }
			    
			    $this.bind('click.ax',function(e){
			    	if(e.target.nodeName!='INPUT')
			    		$('.ax-file-name').trigger('blur');
			    });
	    		$(document).unbind('.ax').bind('keyup.ax',function(e){
	    			if (e.keyCode == 27) {
	    				$('#ax-box-shadow, #ax-box').fadeOut(100);
	    			}  
	    		});
	    			    		
	    		if(!settings.enable)
	    		{
	    			$this.ajaxupload('disable');
	    		}

    	    });
		},
		enable:function()
		{
			return this.each(function()
			{
				var $this		= $(this);
				var settings 	= $this.data('settings');
				settings.enable	= true;
				$this.data('settings', settings);
				$(this).removeClass('ax-disabled').find('input').attr('disabled',false);
			});
		},
		disable:function()
		{
			return this.each(function()
			{
				var $this		= $(this);
				var settings 	= $this.data('settings');
				settings.enable	= false;
				$this.data('settings', settings);
				$(this).addClass('ax-disabled').find('input').attr('disabled',true);
			});
		},
		start:function()
		{
			return this.each(function(){
				var settings = $(this).data('settings');
				startUpload(settings.FILES);
			});
		},
		clear:function()
		{
			return this.each(function(){
				var settings = $(this).data('settings');
				clearQueue(settings);
			});
		},
		destroy : function()
		{
			return this.each(function()
			{
				var settings = $(this).data('settings');
				clearQueue(settings);
				$(this).removeData('settings').html('');
			});
		},
		option : function(option, value)
		{
			return this.each(function(){
				var $this=$(this);
				var settings = $this.data('settings');
			
				if(value != null && value != undefined)
				{
					settings[option] = value;
					$this.data('settings', settings);
					
		    		if(!settings.enable)
		    		{
		    			$this.ajaxupload('disable');
		    		}
		    		else
		    		{
		    			$this.ajaxupload('enable');
		    		}
				}
				else
				{
					return settings[option];
				}
			});
		}
	};

	$.fn.ajaxupload = function(method, options)
	{
		if(methods[method])
		{
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if(typeof method === 'object' || !method)
		{
			return methods.init.apply(this, arguments);
		}
		else
		{
			$.error('Method ' + method + ' does not exist on jQuery.ajaxupload');
		}
	};

})(jQuery);
