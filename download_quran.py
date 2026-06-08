import urllib.request
import json
import os
import sys

def fetch_json(url):
    print(f"Fetching {url}...")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode())

def main():
    try:
        print("Fetching Arabic text...")
        arabic_data = fetch_json("http://api.alquran.cloud/v1/quran/quran-uthmani")
        
        print("Fetching English translation...")
        en_data = fetch_json("http://api.alquran.cloud/v1/quran/en.asad")
        
        print("Fetching Urdu translation...")
        ur_data = fetch_json("http://api.alquran.cloud/v1/quran/ur.jalandhry")

        surahs = arabic_data['data']['surahs']
        en_surahs = en_data['data']['surahs']
        ur_surahs = ur_data['data']['surahs']

        result = []
        for i in range(114):
            surah_ar = surahs[i]
            surah_en = en_surahs[i]
            surah_ur = ur_surahs[i]

            ayahs = []
            for j in range(len(surah_ar['ayahs'])):
                ar_ayah = surah_ar['ayahs'][j]
                en_ayah = surah_en['ayahs'][j]
                ur_ayah = surah_ur['ayahs'][j]

                ayahs.append({
                    "number": ar_ayah['numberInSurah'],
                    "arabic": ar_ayah['text'],
                    "translationEn": en_ayah['text'],
                    "translationUr": ur_ayah['text']
                })

            result.append({
                "number": surah_ar['number'],
                "nameArabic": surah_ar['name'],
                "nameEnglish": surah_ar['englishName'],
                "ayahCount": len(ayahs),
                "ayahs": ayahs
            })

        out_dir = r"D:\Andriod app\PrayerTimes\app\src\main\assets\data"
        os.makedirs(out_dir, exist_ok=True)
        out_path = os.path.join(out_dir, "quran.json")
        
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False)
            
        print(f"Successfully saved to {out_path}")

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
