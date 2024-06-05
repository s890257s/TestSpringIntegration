package tw.pers.allen.demo.config.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.LinkedMultiValueMap;

import tw.pers.allen.demo.service.MyService;

@Configuration
public class ChainedHttpProcessingConfig {

	@Autowired
	private MyService myService;

	/**
	 * 定義一個 HTTP 訊息的生產者，用來接收 HTTP 訊息，並轉換成 Spring Integration 訊息傳送給第一通道。
	 */
	@Bean
	HttpRequestHandlingMessagingGateway chainedHttpInboundGateway() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway();
		gateway.setRequestChannel(firstChannel());

		RequestMapping requestMapping = new RequestMapping();
		requestMapping.setPathPatterns("/greet2");
		requestMapping.setMethods(HttpMethod.GET);
		gateway.setRequestMapping(requestMapping);

		return gateway;
	}

	/**
	 * 定義第一通道，介於生產者與Service之間。<br>
	 * 實作為 DirectChannel，同步訊息通道，訊息在同一個執行緒中傳遞，點對點傳遞訊息。
	 */
	@Bean
	MessageChannel firstChannel() {
		return new DirectChannel();
	}

	/**
	 * 定義一個訊息的內部Service，用來執行內部邏輯。<br>
	 * 完成處理後轉發到第二通道。
	 */
	@Bean
	@ServiceActivator(inputChannel = "firstChannel", outputChannel = "secondChannel")
	MessageHandler messageProcessor() {
		return message -> {
			LinkedMultiValueMap<String, String> params = (LinkedMultiValueMap<String, String>) message.getPayload();

			String name = params.getFirst("name");

			String greetInConsole = myService.sayHi(name);

			System.out.println(greetInConsole);

			params.set("name", greetInConsole);

			MessagingTemplate messagingTemplate = new MessagingTemplate();
			messagingTemplate.send(secondChannel(), new GenericMessage<>(params, message.getHeaders())); // 保留 headers

		};
	}

	/**
	 * 定義第二通道，介於Service與消費者之間。<br>
	 * 實作為 DirectChannel，同步訊息通道，訊息在同一個執行緒中傳遞，點對點傳遞訊息。
	 */
	@Bean
	MessageChannel secondChannel() {
		return new DirectChannel();
	}

	/**
	 * 定義一個回應HTTP的消費者。<br>
	 */
	@Bean
	@ServiceActivator(inputChannel = "secondChannel")
	MessageHandler messageResponder() {
		return message -> {
			LinkedMultiValueMap<String, String> params = (LinkedMultiValueMap<String, String>) message.getPayload();

			MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();

			if (replyChannel != null) {
				replyChannel.send(new GenericMessage<>(params.get("name")));
			}

		};
	}
}