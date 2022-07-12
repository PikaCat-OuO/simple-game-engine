package pikacat;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound {
    private int bufferID;
    private int sourceID;
    private String filePath;

    private boolean isPlaying = false;

    public Sound(String filePath, boolean loops) {
        this.filePath = filePath;

        // 分配空间存放stb的返回信息
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        // 制作音频缓冲
        ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(filePath, channelsBuffer, sampleRateBuffer);
        if (rawAudioBuffer == null) {
            stackPop();
            stackPop();
            System.out.println("错误：不能加载音频'" + filePath + "'");
            return;
        }

        // 从空间中找到stb的返回信息
        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();
        // 释放空间
        stackPop();
        stackPop();

        // 寻找适应的OpenAL格式
        int format = -1;
        switch (channels) {
            case 1 -> format = AL_FORMAT_MONO16;
            case 2 -> format = AL_FORMAT_STEREO16;
        }

        // 生成位置存放音频并上传音频
        bufferID = alGenBuffers();
        alBufferData(bufferID, format, rawAudioBuffer, sampleRate);

        // 生成播放源
        sourceID = alGenSources();

        // 播放配置的设置
        alSourcei(sourceID, AL_BUFFER, bufferID);
        alSourcei(sourceID, AL_LOOPING, loops ? 1 : 0);
        alSourcei(sourceID, AL_POSITION, 0);
        alSourcef(sourceID, AL_GAIN, 0.3f);

        // 释放内存中的音频缓冲区
        free(rawAudioBuffer);
    }

    public void delete() {
        alDeleteSources(sourceID);
        alDeleteBuffers(bufferID);
    }

    // 播放音乐
    public void play() {
        int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
        // 如果没有播放就从头开始播放
        if (state == AL_STOPPED) {
            isPlaying = false;
            alSourcei(sourceID, AL_POSITION, 0);
        }

        if (!isPlaying) {
            alSourcePlay(sourceID);
            isPlaying = true;
        }
    }

    // 停止播放
    public void stop() {
        if (isPlaying) {
            alSourceStop(sourceID);
            isPlaying = false;
        }
    }

    public String getFilePath() {
        return this.filePath;
    }

    public boolean isPlaying() {
        int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
        if (state == AL_STOPPED) {
            isPlaying = false;
        }
        return isPlaying;
    }
}
