import yt_dlp
import os

def download_video(url):
    downloads_path = os.path.join("/sdcard/Download")
    os.makedirs(downloads_path, exist_ok=True)

    ydl_opts = {
        "outtmpl": os.path.join(downloads_path, "%(title)s.%(ext)s"),
        "format": "mp4",
        "quiet": True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])
