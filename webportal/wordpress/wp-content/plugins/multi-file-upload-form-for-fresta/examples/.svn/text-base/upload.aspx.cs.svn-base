using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Collections.Specialized;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.IO;
using System.Drawing;

public partial class upload : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {   
        string filePath = Server.MapPath(Request.QueryString["ax-file-path"]);
        string fileName	= Request.QueryString["ax-file-name"];
        int currByte	= Convert.ToInt32(Request.QueryString["ax-start-byte"]);
        string isLast	= Request.QueryString["isLast"];

        //if set generates thumbs only on images type files
        int thumbHeight		= Convert.ToInt32(Request.QueryString["ax-thumbHeight"]);
        int thumbWidth		= Convert.ToInt32(Request.QueryString["ax-thumbWidth"]);
        string thumbPostfix	= Request.QueryString["ax-thumbPostfix"];
        string thumbPath    = Request.QueryString["ax-thumbPath"];
        string thumbFormat	= Request.QueryString["ax-thumbFormat"];
        string allowExtstr	= Request.QueryString["ax-allow-ext"];
        string[] allowExt 	= allowExtstr.Split('|');


        if (!System.IO.File.Exists(filePath))
        {
            System.IO.Directory.CreateDirectory(filePath);
        }

        if (!System.IO.File.Exists(thumbPath) && thumbPath.Length > 0)
        {
            System.IO.Directory.CreateDirectory(thumbPath);
        }

        if(Request.Files.Count>0)
        {
	        for (int i = 0; i < Request.Files.Count; ++i)
	        {
		        HttpPostedFile file = Request.Files[i];
				string fileNamex = (fileName.length>0)? fileName : file.FileName;
                string fullPath = checkFilename(fileNamex, allowExt, filePath);
			    file.SaveAs(fullPath);
                long size = new FileInfo(fullPath).Length;

                createThumb(fullPath, thumbPath, thumbPostfix, thumbWidth, thumbHeight, thumbFormat);
			    Response.Write(@"{""name"":"""+System.IO.Path.GetFileName(fullPath)+@""",""size"":"""+size.ToString()+@""",""status"":""uploaded"",""info"":""File chunk uploaded""}");
	        }
        }
        else
        {
            string fullPath = (currByte!=0) ? filePath+fileName:checkFilename(fileName, allowExt, filePath);
            // Create a FileStream object to write a stream to a file
            if(fullPath!="error")
	        {
		        FileMode flag	= (currByte==0) ? FileMode.Create : System.IO.FileMode.Append;
                FileStream fileStream = new FileStream(fullPath, flag, System.IO.FileAccess.Write, System.IO.FileShare.None);
                byte[] bytesInStream = new byte[Request.InputStream.Length];
                Request.InputStream.Read(bytesInStream, 0, (int)bytesInStream.Length);
                fileStream.Write(bytesInStream, 0, bytesInStream.Length);
                fileStream.Close();
                
                long size = new FileInfo(fullPath).Length;

                if (isLast.Equals("true"))
                {
                    createThumb(fullPath, thumbPath, thumbPostfix, thumbWidth, thumbHeight, thumbFormat);
                }
                Response.Write(@"{""name"":""" + System.IO.Path.GetFileName(fullPath) + @""",""size"":""" + size.ToString() + @""",""status"":""uploaded"",""info"":""File chunk uploaded""}");
	        }
        }
    }

    public bool createThumb(string filepath, string thumbPath, string postfix, int maxw, int maxh, string format)
    {
        if (maxw <= 0 && maxh <= 0)
	    {
		    return false;
	    }
        string fileExt	= Path.GetExtension(filepath).Replace(".", "");
        string file_name = Path.GetFileName(filepath);
        string thumb_name = file_name + postfix + "." + format;

        if(!String.Equals(fileExt,"jpg",StringComparison.OrdinalIgnoreCase) && !String.Equals(fileExt,"png",StringComparison.OrdinalIgnoreCase) && !String.Equals(fileExt,"gif",StringComparison.OrdinalIgnoreCase))
        {
            return false;
        }

        if (format.Length == 0) format = fileExt;
		if(thumbPath.Length==0)
	    {
		    thumbPath=Path.GetDirectoryName(filepath);	
	    }
        
        if(!thumbPath[thumbPath.Length-1].Equals(@"\") || !thumbPath[thumbPath.Length-1].Equals("/"))
        {
            thumbPath+=Path.DirectorySeparatorChar;
        }

	    System.Drawing.Image img = System.Drawing.Image.FromFile(filepath);
        long ratioX = maxw/img.Width;
        long ratioY = maxh/img.Height;
        long ratio = Math.Min(ratioX, ratioY);
        ratio = (ratio==0)?Math.Max(ratioX,ratioY):ratio;

        int newW=(int)(img.Width*ratio);
        int newH=(int)(img.Height*ratio);

        System.Drawing.Image newImg = img.GetThumbnailImage(newW, newH, null, new System.IntPtr());
        switch(format)
        {
            case "png":
                newImg.Save(thumbPath+thumb_name, System.Drawing.Imaging.ImageFormat.Png);
            break;
            case "gif":
                newImg.Save(thumbPath+thumb_name, System.Drawing.Imaging.ImageFormat.Gif);
            break;
            default:
                newImg.Save(thumbPath+thumb_name, System.Drawing.Imaging.ImageFormat.Jpeg);
            break;
        }
        img.Dispose();
        newImg.Dispose();
        return true;
    }


    public string checkFilename(string filename, string[] allowExt, string filePath)
    {	
	    string[] windowsReserved	= new string[] {"CON", "PRN", "AUX", "NUL","COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};    
	    string[] badWinChars		= new string[] {"<", ">", ":", @"\", "/", "|", "?", "*"};

        for (int i = 0; i < badWinChars.Length; i++)
        {
            filename.Replace(badWinChars[i], "");        
        }
        string fileExt	= System.IO.Path.GetExtension(filename).Replace(".", "");
        string fileBase = System.IO.Path.GetFileNameWithoutExtension(filename);
    
        //check if legal windows file name
        if (Array.IndexOf(windowsReserved, filename) >= 0)
	    {
            Response.Write(@"{""name"":""" + filename + @""",""size"":""0"",""status"":""error"",""info"":""File name not allowed. Windows reserverd.""}");
		    return "error";
	    }
	
        //check if is allowed extension
        if (Array.IndexOf(allowExt, fileExt) < 0 && allowExt.Length > 0)
        {
            if (allowExt.Length != 1 || !String.Equals(allowExt[0], ""))
            {
                Response.Write(@"{""name"":""" + filename + @""",""size"":""0"",""status"":""error"",""info"":""Extension " + fileExt + @" not allowed.""}");
                return "error";
            }
        }

        string fullPath = filePath + filename;
        int c=0;
	    while(System.IO.File.Exists(fullPath))
	    {
		    c++;
            filename = fileBase + "(" + c.ToString() + ")." + fileExt;
            fullPath = filePath + filename;
	    }
	    return fullPath;
    }
}