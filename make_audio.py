import wave, struct, math, os
os.makedirs(r'app/src/main/res/raw', exist_ok=True)
def make_wav(name, freq, dur):
    f=wave.open(name,'w')
    f.setnchannels(1)
    f.setsampwidth(2)
    f.setframerate(44100)
    f.writeframes(b''.join(struct.pack('<h', int(32767.0*math.sin(2*math.pi*freq*(i/44100.0)))) for i in range(int(44100*dur))))
    f.close()
make_wav(r'app/src/main/res/raw/azan_makkah.wav', 440, 2)
make_wav(r'app/src/main/res/raw/azan_madinah.wav', 523.25, 2)
make_wav(r'app/src/main/res/raw/short_beep.wav', 880, 0.5)
