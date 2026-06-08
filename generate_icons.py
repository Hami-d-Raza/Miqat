import os
from PIL import Image

def resize_icon(img_path, output_dir, name, size):
    os.makedirs(output_dir, exist_ok=True)
    try:
        img = Image.open(img_path)
        img = img.resize((size, size), Image.Resampling.LANCZOS)
        img.save(os.path.join(output_dir, name), format="PNG")
    except Exception as e:
        print(f"Failed {size}: {e}")

source_img = r"C:\Users\Adnan LapTop House\.gemini\antigravity-ide\brain\aff32d77-db2b-4cae-96d4-4bd668fdbca6\media__1780648772905.png"
base_res = r"d:\Andriod app\PrayerTimes\app\src\main\res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

fg_sizes = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432
}

print("Generating standard icons...")
for density, size in sizes.items():
    out_dir = os.path.join(base_res, f"mipmap-{density}")
    resize_icon(source_img, out_dir, "ic_launcher.png", size)
    resize_icon(source_img, out_dir, "ic_launcher_round.png", size)

print("Generating foreground icons...")
for density, size in fg_sizes.items():
    out_dir = os.path.join(base_res, f"mipmap-{density}")
    # Resize keeping aspect ratio, pad to transparent if needed
    try:
        os.makedirs(out_dir, exist_ok=True)
        img = Image.open(source_img).convert("RGBA")
        # Scale to 72% of the size so it fits inside the safe zone (72/108 = 0.66)
        fg_size = int(size * 0.66)
        img.thumbnail((fg_size, fg_size), Image.Resampling.LANCZOS)
        bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        offset = ((size - img.width) // 2, (size - img.height) // 2)
        bg.paste(img, offset)
        bg.save(os.path.join(out_dir, "ic_launcher_foreground.png"), format="PNG")
    except Exception as e:
        print(f"Failed foreground {size}: {e}")

print("Done!")
