package tw.pers.allen.demo.config.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.util.ResourceUtils;

@Configuration
public class FileIntegrationConfig {

	// ↓↓以下為生產者配置↓↓

	/**
	 * 定義一個檔案讀取訊息源，監控指定目錄並將新檔案作為訊息傳遞給 fileInputChannel。
	 */
	@Bean
	public FileReadingMessageSource fileReadingMessageSource() {
		FileReadingMessageSource source = new FileReadingMessageSource();

		try {

			source.setDirectory(ResourceUtils.getFile("classpath:data")); // 指定監控的目錄，在此設定為Resource下的data目錄

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CompositeFileListFilter<File> filters = new CompositeFileListFilter<>();
		filters.addFilter(new SimplePatternFileListFilter("*.txt")); // 只讀取 .txt 檔案
		filters.addFilter(new AcceptOnceFileListFilter<>()); // 確保每個檔案只處理一次
		source.setFilter(filters);
		source.setScanEachPoll(true); // 每次掃描整個目錄
		return source;
	}

	/**
	 * 配置一個 Poller(輪詢器)，用於定期檢查目錄中的新檔案。<br>
	 * 這裡設定為每 5 秒檢查一次目錄。
	 */
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata getPoller() {
		PollerMetadata poller = new PollerMetadata();
		poller.setTrigger(new PeriodicTrigger(Duration.ofSeconds(5))); // 使用 Duration 設定每 5 秒檢查一次目錄
		return poller;
	}

	/**
	 * 配置一個 IntegrationFlow，將檔案讀取訊息源連接到 fileInputChannel 通道。<br>
	 * IntegrationFlow 是 Spring Integration Java DSL 的一部分，<br>
	 * 用於構建集成流程(Integration Flow)。它提供了一種直觀的方式來定義訊息的路由和處理邏輯。<br>
	 * 
	 * IntegrationFlow 已經集成在 Spring Integration core 6.3 裡，可直接使用。
	 */
	@Bean
	public IntegrationFlow fileReadingFlow() {
		return IntegrationFlow.from(fileReadingMessageSource(), configurer -> configurer.poller(getPoller()))
				.channel(fileInputChannel()).get();
	}

	// ↑↑以上是生產者配置↑↑

	/**
	 * 定義一個訊息通道，這個通道將用來傳遞檔案讀取的訊息。<br>
	 * 使用 DirectChannel，同步訊息通道，訊息在同一個執行緒中傳遞，點對點傳遞訊息。
	 */
	@Bean
	public MessageChannel fileInputChannel() {
		return new DirectChannel();
	}

	/**
	 * 配置一個訊息處理器來處理從目錄中讀取到的檔案。<br>
	 * 使用 @ServiceActivator 註解將此處理器與 fileInputChannel 通道綁定。
	 */
	@Bean
	@ServiceActivator(inputChannel = "fileInputChannel")
	public MessageHandler fileMessageHandler() {
		return message -> {
			File file = (File) message.getPayload();
			System.out.println("Processing file: " + file.getName() + "(重複檔案不會掃描)");
		};
	}

	// 【若要使用 Spring Integration 寫出檔案，請註解上面方法，解除註解下面方法並輸入路徑】

	/**
	 * 配置一個 FileWritingMessageHandler，用於將訊息寫入檔案。
	 */
	// @Bean
	// @ServiceActivator(inputChannel = "fileInputChannel")
	// public MessageHandler fileWritingMessageHandler() {
	// FileWritingMessageHandler handler = new FileWritingMessageHandler(new
	// File("這邊要輸入檔案輸出的路徑"));
	//
	// handler.setFileExistsMode(FileExistsMode.APPEND); // 檔案存在時追加
	// handler.setExpectReply(false); // 不期待回應
	// handler.setFileNameGenerator(message -> "output.txt"); // 設定檔名
	// return handler;
	// }

}