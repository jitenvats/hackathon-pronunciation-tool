package com.wellsfargo.hackathon.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.Voice;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.util.PronunciationType;

@Service
public class TranslationServiceImpl implements TranslationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationServiceImpl.class);
	
	private List<String> language = new ArrayList<String>();

	@Override
	public ByteString translateEmployeeName(String employeeName, PronunciationType pronunciationType, String language) throws ExternalSystemException {

		LOGGER.info("Employee Name : {}", employeeName );
		LOGGER.info("Pronunciation Type : {}", pronunciationType );
		LOGGER.info("Input Language : {}", language );
		
		language = (null==language) ? "en-US" : language;
		
		LOGGER.info("Translation Language : {}", language );
		
		ByteString audioContents = null;
		TextToSpeechClient textToSpeechClient = null;
		try {
			
			textToSpeechClient = TextToSpeechClient.create();
			SynthesisInput input = SynthesisInput.newBuilder().setText(employeeName).build();
			VoiceSelectionParams.Builder builder = VoiceSelectionParams.newBuilder().setLanguageCode(language);

			switch (pronunciationType) {
			case MALE:
				builder.setSsmlGender(SsmlVoiceGender.MALE);
				LOGGER.info("Translating to Male Voice");
				break;
			case FEMALE:
				builder.setSsmlGender(SsmlVoiceGender.FEMALE);
				LOGGER.info("Translating to Female Voice");
				break;
			default:
				builder.setSsmlGender(SsmlVoiceGender.SSML_VOICE_GENDER_UNSPECIFIED);
				LOGGER.info("Translating to Default Voice");
			}

			VoiceSelectionParams voice = builder.build();

			AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
			LOGGER.info("Translating Employee Name ...");
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			LOGGER.info("Translation Completed");
			audioContents = response.getAudioContent();

		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
			throw new ExternalSystemException("External System failure, Connect after sometime", "E-0003");
		} catch (PermissionDeniedException ex) {
			LOGGER.error(ex.getMessage());
			throw new ExternalSystemException("Internal Server Error, Connect With System Admin", "A-0001");
		} finally {
			if (!textToSpeechClient.isShutdown()) {
				LOGGER.info("Shtdown the Client");
				textToSpeechClient.close();
			}
		}

		return audioContents;
	}

	public List<String> listAllSupportedVoices() throws Exception {
		
		if(!language.isEmpty()) {
			LOGGER.info("Returning Preloaded Data");
			return language;
		}
		
		TextToSpeechClient textToSpeechClient = null;
		try  {
			textToSpeechClient = TextToSpeechClient.create();
			ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();
			ListVoicesResponse response = textToSpeechClient.listVoices(request);
			List<Voice> voices = response.getVoicesList();
			List<String> language = new ArrayList<String>();

			for (Voice voice : voices) {
				List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
			
				for (ByteString languageCode : languageCodes) {
					if(!language.contains(languageCode.toStringUtf8()))
					{
						language.add(languageCode.toStringUtf8());
					}
				}

			}
			
			Collections.sort(language);
			this.language = language;
			return language;
			
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
			throw new ExternalSystemException("External System failure, Connect after sometime", "E-0003");
		} catch (PermissionDeniedException ex) {
			LOGGER.error(ex.getMessage());
			throw new ExternalSystemException("Internal Server Error, Connect With System Admin", "A-0001");
		} finally {
			if (!textToSpeechClient.isShutdown()) {
				LOGGER.info("Shtdown the Client");
				textToSpeechClient.close();
			}
		}
	}

}
