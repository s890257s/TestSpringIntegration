package tw.pers.allen.demo.config.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.LinkedMultiValueMap;

import tw.pers.allen.demo.service.MyService;

@Configuration
public class ProcessedBasicHttpConfig {

	@Autowired
	private MyService myService;

	/**
	 * 定義一個 HTTP 訊息的生產者，用來接收 HTTP 訊息，並轉換成 Spring Integration 訊息傳送給 Channel。
	 */
	@Bean
	HttpRequestHandlingMessagingGateway basicHttpInboundGateway() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway();
		gateway.setRequestChannel(httpChannel());

		RequestMapping requestMapping = new RequestMapping();
		requestMapping.setPathPatterns("/greet");
		requestMapping.setMethods(HttpMethod.GET);
		gateway.setRequestMapping(requestMapping);

		return gateway;
	}

	/**
	 * 定義一個訊息通道，介於生產者與消費者之間。<br>
	 * 實作為 DirectChannel，同步訊息通道，訊息在同一個執行緒中傳遞，點對點傳遞訊息。
	 */
	@Bean
	MessageChannel httpChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "httpChannel")
	MessageHandler messageHandler() {
		return message -> {
			LinkedMultiValueMap<String, String> params = (LinkedMultiValueMap<String, String>) message.getPayload();

			String name = params.getFirst("name");

			String greet = myService.sayHi(name);

			MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();
			if (replyChannel != null) {
				replyChannel.send(new GenericMessage<>(greet));
			}

		};
	}

}