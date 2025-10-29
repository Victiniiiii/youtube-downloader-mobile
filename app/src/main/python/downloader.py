import os
import yt_dlp

try:
    yt_dlp_version = yt_dlp.version.__version__
except Exception as e:
    yt_dlp_version = f"Error: {str(e)}"

def download_video(url, download_audio=False):
    downloads_path = "/sdcard/Download"
    os.makedirs(downloads_path, exist_ok=True)

    if download_audio:
        ydl_opts = {
            "outtmpl": os.path.join(downloads_path, "%(title)s.%(ext)s"),
            "format": "bestaudio[ext=m4a]/bestaudio[ext=aac]/bestaudio",
            "quiet": False,
            "no_warnings": False,
            "extract_audio": False,
            "prefer_free_formats": False,
        }
    else:
        ydl_opts = {
            "outtmpl": os.path.join(downloads_path, "%(title)s.%(ext)s"),
            "format": "best[ext=mp4]/best",
            "quiet": False,
            "no_warnings": False,
        }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            if info:
                filename = ydl.prepare_filename(info)
                if os.path.exists(filename) and os.path.getsize(filename) > 0:
                    return f"Downloaded: {os.path.basename(filename)}"
                else:
                    return "Error: Downloaded file is empty or does not exist"
            return "Error: Could not extract video information"
    except Exception as e:
        return f"Error: {str(e)}"


def get_ytdlp_version():
    return yt_dlp_version


def get_supported_sites():
    popular_sites = [
        "YouTube",
        "TikTok",
        "SoundCloud",
        "Instagram",
        "Facebook",
        "Twitter",
        "Twitch",
        "Reddit",
    ]
    return "\n".join(popular_sites)