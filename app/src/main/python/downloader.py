import os
import yt_dlp

try:
    yt_dlp_version = yt_dlp.version.__version__
except Exception as e:
    yt_dlp_version = f"Error: {str(e)}"

def download_video(url, download_audio=False, folder="/sdcard/Download"):
    os.makedirs(folder, exist_ok=True)

    if download_audio:
        ydl_opts = {
            "outtmpl": os.path.join(folder, "%(title)s.%(ext)s"),
            "format": "bestaudio[ext=m4a]/bestaudio[ext=aac]/bestaudio",
            "quiet": False,
            "no_warnings": False,
        }
    else:
        ydl_opts = {
            "outtmpl": os.path.join(folder, "%(title)s.%(ext)s"),
            "format": "best[ext=mp4]/best",
            "quiet": False,
            "no_warnings": False,
        }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            if "entries" in info:
                downloaded = []
                for entry in info["entries"]:
                    if not entry:
                        continue
                    filename = ydl.prepare_filename(entry)
                    if os.path.exists(filename) and os.path.getsize(filename) > 0:
                        downloaded.append(os.path.basename(filename))
                return f"Downloaded {len(downloaded)} items" if downloaded else "No videos downloaded"
            else:
                filename = ydl.prepare_filename(info)
                if os.path.exists(filename) and os.path.getsize(filename) > 0:
                    return f"Downloaded: {os.path.basename(filename)}"
                return "Error: Downloaded file is empty or does not exist"
    except Exception as e:
        return f"Error: {str(e)}"


def get_ytdlp_version():
    return yt_dlp_version