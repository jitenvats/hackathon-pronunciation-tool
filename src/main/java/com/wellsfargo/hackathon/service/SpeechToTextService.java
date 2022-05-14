package com.wellsfargo.hackathon.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.wellsfargo.hackathon.exception.ExternalSystemException;

public class SpeechToTextService {

	public static String streamingTranscribeWithAutomaticPunctuation(byte[] data, String language, long speed)
			throws ExternalSystemException {
		SpeechClient speech = null;
		try {

			long hertz = 16000 * speed;

			speech = SpeechClient.create();
			RecognitionConfig recConfig = RecognitionConfig.newBuilder().setEncoding(AudioEncoding.LINEAR16)
					.setLanguageCode(language).setSampleRateHertz((int) hertz)
//					.setEnableAutomaticPunctuation(true)
					.build();

			StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder().setConfig(recConfig).build();

			class ResponseApiStreamingObserver<T> implements ApiStreamObserver<T> {
				private final SettableFuture<List<T>> future = SettableFuture.create();
				private final List<T> messages = new java.util.ArrayList<T>();

				@Override
				public void onNext(T message) {
					messages.add(message);
				}

				@Override
				public void onError(Throwable t) {
					future.setException(t);
				}

				@Override
				public void onCompleted() {
					future.set(messages);
				}

				public SettableFuture<List<T>> future() {
					return future;
				}
			}

			ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver = new ResponseApiStreamingObserver<>();

			BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable = speech
					.streamingRecognizeCallable();

			ApiStreamObserver<StreamingRecognizeRequest> requestObserver = callable.bidiStreamingCall(responseObserver);

			requestObserver.onNext(StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build());

			requestObserver
					.onNext(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build());

			requestObserver.onCompleted();

			List<StreamingRecognizeResponse> responses = responseObserver.future().get();
			String translation = null;

			for (StreamingRecognizeResponse response : responses) {
				StreamingRecognitionResult result = response.getResultsList().get(0);
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);

				translation = new String(alternative.getTranscript().getBytes(StandardCharsets.UTF_8),
						StandardCharsets.UTF_8);
				System.out.printf("translation : %s\n", translation);

				String asciiEncodedString = new String(alternative.getTranscript().getBytes(StandardCharsets.US_ASCII),
						StandardCharsets.US_ASCII);
				System.out.printf("asciiEncodedString : %s\n", asciiEncodedString);

				String utf16 = new String(alternative.getTranscript().getBytes(StandardCharsets.ISO_8859_1),
						StandardCharsets.ISO_8859_1);
				System.out.printf("utf16 : %s\n", utf16);

				System.out.printf("URLDecoder : %s\n", URLDecoder
						.decode(new String(alternative.getTranscript().getBytes("ISO-8859-1"), "UTF-8"), "UTF-8"));

			}

			return translation;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExternalSystemException("External System failure, Connect after sometime", "E-0003");
		}finally {
			if(speech.isShutdown()) {
				speech.close();
			}
		}
	}

	public static String  toText (ByteString content, String language) throws ExternalSystemException {
		 SpeechClient speechClient = null;
		    try {
		    	speechClient = SpeechClient.create();

		      RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();

		      RecognitionConfig config =
		          RecognitionConfig.newBuilder()		       
		              .setEncoding(AudioEncoding.LINEAR16)
		              .setSampleRateHertz(24000)
		              .setEnableAutomaticPunctuation(true)
		              .setLanguageCode(language)
		              .build();
		      
		      RecognizeResponse response = speechClient.recognize(config, audio);
		      List<SpeechRecognitionResult> results = response.getResultsList();

		      String text = null;
		      for (SpeechRecognitionResult result : results) {
		        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
		        text = alternative.getTranscript();
		        System.out.printf("Transcription: %s%n", alternative.getTranscript());
		      }
		        return text;

		    }catch (Exception ex) {
		    	throw new ExternalSystemException("External System failure, Connect after sometime", "E-0003");
		    }finally {
		    	if(speechClient.isShutdown()) {
		    		speechClient.close();		
		    	}
		    }
		  }

}
