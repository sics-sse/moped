/**
 * Copyright (c) 2012-2013 Dawid Kraczkowski. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
jQuery(document).ready(function($) {
    var ajaxUploadIframe;
    $.fn.ajaxUpload = function(options) {
        var settings = $.extend({
            accept : ['*'],
            name: 'file',
            method: 'POST',
            url: '/',
            data: false,
            onSubmit: function(){
                return true;
            },
            onComplete: function(){
                return true;
            }
        },options);

        //Iterate over the current set of matched elements
        return this.each(function() {
            //create form
            var button = $(this);
            button.css('position','relative');
            button.setData = function(data) {
                settings.data = data;
            }


            var form = $('<form style="margin: 0px !important; padding: 0px !important; position: absolute; top: 0px; left: 0px;"' +
                ' method="' + settings.method + '" enctype="multipart/form-data" action="' + settings.url +'">' +
                ' <input name="' + settings.name + '" type="file" /></form>');

            var input = form.find('input[name=' + settings.name + ']');
            input.css('display','block');
            input.css('overflow','hidden');

            input.css('width','100%');
            input.css('height','100%');
            input.css('text-align','right');
            input.css('opacity','0');
            input.css('z-index','999999');

            input.change(function(e){
                form.find('input[type=hidden]').remove();
                settings.onSubmit.call(button, $(this));
                //add data
                if (settings.data) {
                    $.each(settings.data, function(n,v){
                        form.append($('<input type="hidden" name="' + n +'" value="' + v +'">'));
                    });
                }

                form.submit();
                $(form).find('input[type=file]').attr('disabled','disabled');

            });

            $(button).append(form);

            //check if iframe exists
            if (!ajaxUploadIframe) {
                ajaxUploadIframe = $('<iframe id="__ajaxUploadIFRAME" name="__ajaxUploadIFRAME"></iframe>').attr('style','style="width:0px;height:0px;border:0px solid #fff;"').hide();
                ajaxUploadIframe.attr('src', '');
                $(document.body).append(ajaxUploadIframe);
            }

            var onUpload = function(){
                $(form).find('input[type=file]').removeAttr('disabled');
                var response = $(this).contents().find('html body').text();
                settings.onComplete.call(button, response);
                ajaxUploadIframe.unbind();
            };



            //on file submit
            form.submit(function(e){
                //set iframe onload event
                ajaxUploadIframe.load(onUpload);
                form.attr('target','__ajaxUploadIFRAME');
                e.stopPropagation();

            });

        });
    }

})(jQuery);