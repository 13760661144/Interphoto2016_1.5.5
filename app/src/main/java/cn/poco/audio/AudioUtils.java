package cn.poco.audio;

import android.support.annotation.IntDef;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/5/19
 * 封装一个音频工具类，方便调用底层函数处理
 */
public class AudioUtils {

	public static final int AUDIO_TYPE_WAV = 1;
	public static final int AUDIO_TYPE_PCM = 1 << 1;
	public static final int AUDIO_TYPE_AAC = 1 << 2;

	@IntDef( {AUDIO_TYPE_WAV, AUDIO_TYPE_PCM, AUDIO_TYPE_AAC})
	@Retention(RetentionPolicy.SOURCE)
	@interface AudioType {

	}

	private AudioUtils() {

	}

	/**
	 * 变速、变调、既变调又变速
	 *
	 * @param aacFilePath aac文件路径
	 * @param outputPath  结果输出路径
	 * @param spend       变调变速 （不改变设置为1）
	 * @param pitch       变调 （不改变设置为0）
	 * @param tempo       变速 （不改变设置为1）
	 * @param outputType  输出文件类型
	 * @return 是否成功
	 */
	public static boolean changeAacSound(String aacFilePath, String outputPath, float spend, float pitch, float tempo, @AudioType int outputType) {
		int result;

		String wavTempFile = FileUtils.getTempPath(FileUtils.WAV_FORMAT);

		result = AacWavUtils.aacToWav(aacFilePath, wavTempFile);

		if (result < 0) {
			FileUtils.delete(wavTempFile);
			return false;
		}

		FileUtils.delete(outputPath);
		boolean resultB;

		if (outputType == AUDIO_TYPE_WAV) { // wav
			resultB = changeWavSound(wavTempFile, outputPath, spend, pitch, tempo, outputType);
			FileUtils.delete(wavTempFile);
			if (!resultB) {
				FileUtils.delete(outputPath);
				return false;
			}
			return true;
		} else if (outputType == AUDIO_TYPE_PCM) { // pcm
			resultB = changeWavSound(wavTempFile, outputPath, spend, pitch, tempo, outputType);
			FileUtils.delete(wavTempFile);
			if (!resultB) {
				FileUtils.delete(outputPath);
				return false;
			}

			return true;
		} else if (outputType == AUDIO_TYPE_AAC) { // aac
			resultB = changeWavSound(wavTempFile, outputPath, spend, pitch, tempo, outputType);
			FileUtils.delete(wavTempFile);
			if (!resultB) {
				FileUtils.delete(outputPath);
				return false;
			}

			return true;
		}

		FileUtils.delete(wavTempFile);
		FileUtils.delete(outputPath);

		return false;
	}

	/**
	 * 变速、变调、既变调又变速
	 *
	 * @param inputWavPath wav文件路径
	 * @param outputPath   结果输出路径
	 * @param spend        变调变速 （不改变设置为1）
	 * @param pitch        变调 （不改变设置为0）
	 * @param tempo        变速 （不改变设置为1）
	 * @param outputType   输出文件类型
	 * @return 是否成功
	 */
	public static boolean changeWavSound(String inputWavPath, String outputPath, float spend, float pitch, float tempo, @AudioType int outputType) {
		int result;

		String tempOutputWavFile = FileUtils.getTempPath(FileUtils.WAV_FORMAT);

//		HashMap<String, Integer> wavInfoMap = getWavInfo(inputWavPath);
//		long sampleRate = wavInfoMap.get("samplerate") > 0 ? wavInfoMap.get("samplerate") : 44100;
//		int channels = wavInfoMap.get("channels") > 0 ? wavInfoMap.get("channels") : 2;

		SoundFactory soundFactory = new SoundFactory();
		soundFactory.setSpeed(spend);
		soundFactory.setPitchSemiTones(pitch);
		soundFactory.setTempo(tempo);
		result = soundFactory.processFile(inputWavPath, tempOutputWavFile);

		if (result < 0) {
			FileUtils.delete(tempOutputWavFile);
			return false;
		}

		FileUtils.delete(outputPath);

		//wav
		if (outputType == AUDIO_TYPE_WAV) { // wav
			return new File(tempOutputWavFile).renameTo(new File(outputPath));
		} else if (outputType == AUDIO_TYPE_PCM) { // pcm
			result = PcmWav.wavToPcm(tempOutputWavFile, outputPath);
			FileUtils.delete(tempOutputWavFile);
			if (result < 0) {
				FileUtils.delete(outputPath);
				return false;
			}
			return true;
		} else if (outputType == AUDIO_TYPE_AAC) { // aac
			result = AacWavUtils.wavToAac(tempOutputWavFile, outputPath);
			FileUtils.delete(tempOutputWavFile);
			if (result < 0) {
				FileUtils.delete(outputPath);
				return false;
			}
			return true;
		}

		FileUtils.delete(tempOutputWavFile);
		FileUtils.delete(outputPath);
		return false;
	}

	/**
	 * 混合音频，支持调节音量比重（音量从[0,1], 1是原始音量大小，小于1则使音量变小，0为无声）
	 *
	 * @param inputFilePath   音频输入路径
	 * @param volume          主音频的音量
	 * @param outputFilePath  音频输出路径
	 * @param bgMusicPathList 背景音乐路径列表
	 * @param volumeList      背景音乐的音量
	 * @param isRepeat        背景音乐是否重复播放
	 * @param bgMusicStartEnd 背景音频列表对应的起始、终止位置在主文件的百分百，如0.234.
	 * @return 是否成功
	 */
	private static boolean mixAudio(String inputFilePath, double volume, String outputFilePath, List<String> bgMusicPathList, List<Double> volumeList, boolean isRepeat, double... bgMusicStartEnd) {
		if (bgMusicPathList == null || bgMusicPathList.size() == 0 || bgMusicStartEnd == null || bgMusicStartEnd.length == 0 || bgMusicPathList.size() * 2 != bgMusicStartEnd.length || bgMusicPathList.size() != volumeList.size()) {
			return false;
		}

		String tempMixPcm;

		List<String> tempMixPcmList = new ArrayList<>();
		double start, end;

		if (bgMusicPathList.size() == 1) {
			int reslut;
			tempMixPcm = outputFilePath;
			start = bgMusicStartEnd[0];
			end = bgMusicStartEnd[1];
			if (isRepeat) {
				reslut = PcmMix.mixPcmVloAdjustRepeat(inputFilePath, bgMusicPathList.get(0), tempMixPcm, start, end, volume, volumeList.get(0));
			} else {
				reslut = PcmMix.mixPcmVloAdjust(inputFilePath, bgMusicPathList.get(0), tempMixPcm, start, end, volume, volumeList.get(0));
			}
			if (reslut < 0) {
				FileUtils.delete(tempMixPcm);
				return false;
			}
		} else {
			for (int i = 0; i < bgMusicPathList.size(); i++) {
				tempMixPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
				int reslut;

				start = bgMusicStartEnd[i * 2];
				end = bgMusicStartEnd[i * 2 + 1];

				if (i == 0) {
					tempMixPcmList.add(tempMixPcm);
					if (isRepeat) {
						reslut = PcmMix.mixPcmVloAdjustRepeat(inputFilePath, bgMusicPathList.get(0), tempMixPcm, start, end, volume, volumeList.get(0));
					} else {
						reslut = PcmMix.mixPcmVloAdjust(inputFilePath, bgMusicPathList.get(0), tempMixPcm, start, end, volume, volumeList.get(0));
					}
					if (reslut < 0) {
						return false;
					}
				} else {
					if (i == bgMusicPathList.size() - 1) {
						tempMixPcm = outputFilePath;
					} else {
						tempMixPcmList.add(tempMixPcm);
					}
					if (isRepeat) {
						reslut = PcmMix.mixPcmVloAdjustRepeat(tempMixPcmList.get(i - 1), bgMusicPathList.get(i), tempMixPcm, start, end, 1, volumeList.get(i));
					} else {
						reslut = PcmMix.mixPcmVloAdjust(tempMixPcmList.get(i - 1), bgMusicPathList.get(i), tempMixPcm, start, end, 1, volumeList.get(i));
					}
					if (reslut < 0) {
						FileUtils.delete(tempMixPcmList.get(i - 1));
						return false;
					}
				}
			}

			for (String s : tempMixPcmList) {
				FileUtils.delete(s);
			}
		}
		return true;
	}

	/**
	 * 获取最小的采样率，以作为目标采样率，进行重采样
	 *
	 * @param inputpath 主音频文件路径
	 * @param musicList 要在主文件上混入声音的音频文件列表
	 * @return 采样率
	 */
	private static int getMinSamplerate(String inputpath, List<String> musicList) {
		int min = CommonUtils.getAudioSampleRate(inputpath);
		for (int i = 0; i < musicList.size(); i++) {
			int samplerate = CommonUtils.getAudioSampleRate(musicList.get(i));
			min = samplerate < min ? samplerate : min;
		}
		return min;
	}

	/**
	 * 音频文件混音（格式WAV MP3 AAC）（音量从[0,1], 1是原始音量大小，小于1则使音量变小,0为无声）
	 *
	 * @param inputFilePath   主音频文件路径
	 * @param volume          主音频的音量
	 * @param outputFilePath  输出音频文件路径
	 * @param bgMusicPathList 要在主文件上混入声音的音频文件列表
	 * @param volumeList      bgMusicPathList的音量
	 * @param isRepeat        背景音乐是否重复播放
	 * @param bgMusicStartEnd bgMusicPathList的基于主音频文件混入起始位置列表
	 */
	public static boolean mixAudioNew(String inputFilePath, double volume, String outputFilePath, List<String> bgMusicPathList, List<Double> volumeList, boolean isRepeat, double... bgMusicStartEnd) {
		if (bgMusicPathList == null || bgMusicPathList.size() == 0 || bgMusicStartEnd == null || bgMusicPathList.size() * 2 != bgMusicStartEnd.length) {
			return false;
		}

		String outPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		int commonSampleRate;
		int commonChannels;

		int mainFileSampleRate = getMinSamplerate(inputFilePath, bgMusicPathList);
		int mianFileChannels = CommonUtils.getAudioChannels(inputFilePath);
		if (mainFileSampleRate > 0) {
			commonSampleRate = mainFileSampleRate;
		} else {
			return false;
		}

		if (mianFileChannels > 0) {
			commonChannels = mianFileChannels;
		} else {
			return false;
		}

		String inputFilePcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		if (!CommonUtils.getAudioPcm(inputFilePath, inputFilePcm)) {
			FileUtils.delete(inputFilePcm);
			return false;
		}

		List<String> bgListPcm = new ArrayList<>();
		List<Integer> bgSampleRateList = new ArrayList<>();
		List<Integer> bgChannelsList = new ArrayList<>();
		for (int i = 0; i < bgMusicPathList.size(); i++) {
			int tempSR = CommonUtils.getAudioSampleRate(bgMusicPathList.get(i));
			int tempC = CommonUtils.getAudioChannels(bgMusicPathList.get(i));
			bgSampleRateList.add(tempSR);
			bgChannelsList.add(tempC);

			if (tempSR < 1) {
				return false;
			}

			if (tempC < 1) {
				return false;
			}

			String tempPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
			boolean isOk = CommonUtils.getAudioPcm(bgMusicPathList.get(i), tempPcm);
			if (!isOk) {
				return false;
			}

			// do resample
			if (tempSR == commonSampleRate) { // same sample rate
				if (tempC == commonChannels) {
					bgListPcm.add(tempPcm);
				} else {
					String temp = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
					Resample.doReChannels(tempPcm, temp, tempC, commonChannels);
					bgListPcm.add(temp);
				}
			} else {                                    //diff sample rate
				if (tempC == commonChannels) {
					String temp = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
					Resample.doResample(tempPcm, temp, tempSR, commonSampleRate);
					bgListPcm.add(temp);
				} else {                              // diff sample rate  and channels
					String temps = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
					Resample.doResample(tempPcm, temps, tempSR, commonSampleRate);
					String tempc = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
					Resample.doReChannels(temps, tempc, tempC, commonChannels);
					bgListPcm.add(tempc);
				}

			}
		}

		boolean misRet = mixAudio(inputFilePcm, volume, outPcm, bgListPcm, volumeList, isRepeat, bgMusicStartEnd);

		if (!misRet) {
			return false;
		}

		return CommonUtils.encodeAudio(outPcm, outputFilePath, commonSampleRate, commonChannels);

	}

	/**
	 * 获取wac文件信息
	 * 1.channels  2.samplerate
	 *
	 * @param wavFilePath wac文件路径
	 * @return wac文件信息
	 */
	private static HashMap<String, Integer> getWavInfo(String wavFilePath) {
		File f = new File(wavFilePath);

		HashMap<String, Integer> wavInfoMap = new HashMap<>();

		try {
			FileInputStream stream = new FileInputStream(f);

			byte[] header = new byte[12];
			stream.read(header, 0, 12);
			if (header[0] != 'R' || header[1] != 'I' || header[2] != 'F' || header[3] != 'F' || header[8] != 'W' || header[9] != 'A' || header[10] != 'V' || header[11] != 'E') {
				return wavInfoMap;
			}

			byte[] chunkHeader = new byte[8];
			stream.read(chunkHeader, 0, 8);

			int chunkLen = ((0xff & chunkHeader[7]) << 24) | ((0xff & chunkHeader[6]) << 16) | ((0xff & chunkHeader[5]) << 8) | ((0xff & chunkHeader[4]));

			if (chunkHeader[0] == 'f' && chunkHeader[1] == 'm' && chunkHeader[2] == 't' && chunkHeader[3] == ' ') {
				if (chunkLen < 16 || chunkLen > 1024) {

				}

				byte[] fmt = new byte[chunkLen];
				stream.read(fmt, 0, chunkLen);

				int format = ((0xff & fmt[1]) << 8) | ((0xff & fmt[0]));
				int mChannels = ((0xff & fmt[3]) << 8) | ((0xff & fmt[2]));
				int mSampleRate = ((0xff & fmt[7]) << 24) | ((0xff & fmt[6]) << 16) | ((0xff & fmt[5]) << 8) | ((0xff & fmt[4]));
				wavInfoMap.put("channels", mChannels);
				wavInfoMap.put("samplerate", mSampleRate);
				wavInfoMap.put("format", format);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wavInfoMap;
	}

	/**
	 * mp3解码成pcm
	 *
	 * @param mp3Path    mp3文件路径
	 * @param outputPath pcm文件输出路径
	 * @return 是否成功
	 */
	public static boolean decodeMp3(String mp3Path, String outputPath) {
		if (MP3DeEncode.decode(mp3Path, outputPath) < 0) {
			FileUtils.delete(outputPath);
			return false;
		}

		return true;
	}

	/**
	 * 填充空白延长音频文件,WAV MP3 AAC
	 *
	 * @param inputPath  WAV MP3 AAC 任意格式，必须包含正确后缀
	 * @param outputPath WAV MP3 AAC 任意格式，必须包含正确后缀
	 * @param duration   延长后音频文件总时长 （单位：秒）
	 * @param start      原有声音频文件在延长后的文件的某一开始位置 （单位：秒）
	 * @param end        原有声音频文件在延长后的文件的某一结束位置 （单位：秒）
	 * @return
	 */
	public static boolean expandAudioDuration(String inputPath, String outputPath, double duration, double start, double end) {

		int sampleRate = CommonUtils.getAudioSampleRate(inputPath);
		int channels = CommonUtils.getAudioChannels(inputPath);
		double statDuration = start;
		double endDuration = duration - end;

		if (sampleRate < 1 || channels < 1) {
			return false;
		}

		if (start == 0 && end == duration) {
			return false;
		}

		//在后面扩大
		if (start == 0) {
			String endPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			boolean ret = GenerateMuteAudio.generteMuteWav(sampleRate, channels, 16, endDuration, endPartWav);
			if (!ret) {
				FileUtils.delete(endPartWav);
				return false;
			}

			String inputPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			ret = CommonUtils.audioToWav(inputPath, inputPartWav);
			if (!ret) {
				FileUtils.delete(inputPartWav);
				return false;
			}

			String outputWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			int result = SoundJoint.joint(inputPartWav, endPartWav, outputWav);
			if (result < 0) {
				FileUtils.delete(inputPartWav);
				FileUtils.delete(endPartWav);
				FileUtils.delete(outputWav);
				return false;
			}

			ret = CommonUtils.wavToAudio(outputWav, outputPath);
			if (!ret) {
				FileUtils.delete(outputPath);
				return false;
			}

			//在前面扩大
		} else if (end == duration) {
			String startPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			boolean ret = GenerateMuteAudio.generteMuteWav(sampleRate, channels, 16, statDuration, startPartWav);
			if (!ret) {
				FileUtils.delete(startPartWav);
				return false;
			}

			String inputPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			ret = CommonUtils.audioToWav(inputPath, inputPartWav);
			if (!ret) {
				FileUtils.delete(inputPartWav);
				return false;
			}

			String outputWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			int result = SoundJoint.joint(startPartWav, inputPartWav, outputWav);
			if (result < 0) {
				FileUtils.delete(startPartWav);
				FileUtils.delete(inputPartWav);
				FileUtils.delete(outputWav);
				return false;
			}

			ret = CommonUtils.wavToAudio(outputWav, outputPath);
			if (!ret) {
				FileUtils.delete(outputPath);
				return false;
			}

			//两头扩大
		} else {
			String startPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			boolean ret = GenerateMuteAudio.generteMuteWav(sampleRate, channels, 16, statDuration, startPartWav);
			if (!ret) {
				FileUtils.delete(startPartWav);
				return false;
			}
			String endPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			ret = GenerateMuteAudio.generteMuteWav(sampleRate, channels, 16, endDuration, endPartWav);
			if (!ret) {
				FileUtils.delete(endPartWav);
				return false;
			}

			String inputPartWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			ret = CommonUtils.audioToWav(inputPath, inputPartWav);
			if (!ret) {
				FileUtils.delete(inputPartWav);
				return false;
			}

			String outputWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			List<String> inputWavList = new ArrayList<>();
			inputWavList.add(startPartWav);
			inputWavList.add(inputPartWav);
			inputWavList.add(endPartWav);
			int result = SoundJoint.joint(outputWav, inputWavList);
			if (result < 0) {
				FileUtils.delete(startPartWav);
				FileUtils.delete(inputPartWav);
				FileUtils.delete(endPartWav);
				FileUtils.delete(outputWav);
				return false;
			}

			ret = CommonUtils.wavToAudio(outputWav, outputPath);
			if (!ret) {
				FileUtils.delete(outputPath);
				return false;
			}
		}

		return true;
	}

	/**
	 * aac转wav
	 */
	public static int aacToWav(String inputFilePath, String outputFilePath) {
		int result;
		String inputTempPCM = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		result = AacEnDecoder.decodeAAC1(inputFilePath, inputTempPCM);
		if (result < 0) {
			FileUtils.delete(inputTempPCM);
			return result;
		}
		long samplerate = AacEnDecoder.getSamplerate(inputFilePath);
		int channels = AacEnDecoder.getChannels(inputFilePath);

		if (samplerate < 0) {
			FileUtils.delete(inputTempPCM);
			return -1;
		}
		boolean resultPW = PcmToWav.pcmToWav(inputTempPCM, outputFilePath, samplerate, channels);
		if (!resultPW) {
			FileUtils.delete(inputTempPCM);
			FileUtils.delete(outputFilePath);
			return result;
		}

		FileUtils.delete(inputTempPCM);
		return result;
	}

	/**
	 * wav转aac
	 */
	public static int wavToAac(String inputFilePath, String outputFilePath) {
		int result = -1;
		String inputTempPCM = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		result = PcmWav.wavToPcm(inputFilePath, inputTempPCM);
		if (result < 0) {
			FileUtils.delete(inputTempPCM);
			return result;
		}
		int[] wavInfo = SoundJoint.getWavHead(inputFilePath);
		if (wavInfo == null || wavInfo.length == 0) {
			return -1;
		}
		long samplerate = wavInfo[0];
		int channels = wavInfo[1];
		int bit = wavInfo[2];
		if (samplerate < 0 || channels < 0) {
			FileUtils.delete(inputTempPCM);
			return -1;
		}

		result = AacEnDecoder.encodeAAC(samplerate, channels, bit, inputTempPCM, outputFilePath);
		FileUtils.delete(inputTempPCM);
		if (result < 0) {
			FileUtils.delete(outputFilePath);
			return result;
		}

		return result;
	}

	/**
	 * mp3转wav
	 */
	public static boolean mp3ToWav(String inputFilePath, String outputFilePath) {
		int sampleRate = MP3DeEncode.getSamplerate(inputFilePath);
		int channels = MP3DeEncode.getChannels(inputFilePath);
		if (sampleRate < 1 || channels < 1) {
			return false;
		}
		int result;
		String tempPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		result = MP3DeEncode.decode(inputFilePath, tempPcm);
		if (result < 0) {
			FileUtils.delete(tempPcm);
			return false;
		}
		boolean ret = PcmToWav.pcmToWav(tempPcm, outputFilePath, sampleRate, channels);
		if (!ret) {
			FileUtils.delete(tempPcm);
			FileUtils.delete(outputFilePath);
			return false;
		}
		return true;
	}

	/**
	 * wav转mp3
	 */
	public static boolean wavToMp3(String inputFilePath, String outputFilePath) {

		int[] head = SoundJoint.getWavHead(inputFilePath);

		if (head == null || head.length < 2) {
			return false;
		}
		int sampleRate = head[0];
		int channels = head[1];
		if (sampleRate < 1 || channels < 1) {
			return false;
		}
		int result = -1;
		String tempPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		result = PcmWav.wavToPcm(inputFilePath, tempPcm);
		if (result < 0) {
			FileUtils.delete(tempPcm);
			return false;
		}
		result = MP3DeEncode.encode(tempPcm, outputFilePath, sampleRate, channels);
		if (result < 0) {
			FileUtils.delete(tempPcm);
			FileUtils.delete(outputFilePath);
			return false;
		}
		return true;
	}

	/**
	 * 音量调节(WAV ,AAC ,MP3)
	 *
	 * @param inputFile
	 * @param outputFile
	 * @param volume     范围：[0,1],原声：1
	 * @return
	 */
	public static boolean volumeAdjust(String inputFile, String outputFile, double volume) {

		String tempPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		String tempOutPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		int sampleRate = CommonUtils.getAudioSampleRate(inputFile);
		int channels = CommonUtils.getAudioChannels(inputFile);
		boolean result;
		result = CommonUtils.getAudioPcm(inputFile, tempPcm);
		if (!result || sampleRate < 1 || channels < 1) {
			return false;
		}

		int ret = -1;
		ret = PcmMix.volAdjust(tempPcm, tempOutPcm, volume);

		if (ret < 0) {
			return false;
		}

		result = CommonUtils.encodeAudio(tempOutPcm, outputFile, sampleRate, channels);
		if (!result) {
			return false;
		}

		return true;
	}

	/**
	 * 音频片段拼接成一段音频 MP3 WAV AAC 任意格式输出输出
	 *
	 * @param inputFileList 输入音频路径列表
	 * @param outputFilePath 输出音频路径
	 * @return 是否成功
	 */
	public static boolean jointAuido(List<String> inputFileList, String outputFilePath) {
		boolean resultB;

		int blockNum = inputFileList.size();

		int commonSampleRate = 44100;
		int commonChannels = 2;

		List<String> wavList = new ArrayList<>();
		for (int i = 0; i < blockNum; i++) {
			String tempWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
			resultB = CommonUtils.audioToWav(inputFileList.get(i), tempWav, commonSampleRate, commonChannels);
			if (!resultB) {
				return false;
			}
			wavList.add(tempWav);
		}

		String tempOutWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
		resultB = CommonUtils.jointWav(wavList, tempOutWav);
		if (!resultB) {
			FileUtils.delete(tempOutWav);
			return false;
		}

		resultB = CommonUtils.wavToAudio(tempOutWav, outputFilePath);
		if (!resultB) {
			FileUtils.delete(tempOutWav);
			FileUtils.delete(outputFilePath);
			return false;
		}
		return true;
	}
}
