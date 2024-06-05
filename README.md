# Spring Integration

## 簡介

- Spring Integration 是 Spring Framework 的一個擴展模組。使用 Springboot 的情況，可直接加入 Starter，並依處理需求加入額外組件。

  ```xml
      <!-- Spring Boot Integration Starter (核心) -->
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-integration</artifactId>
      </dependency>

      <!-- Spring Integration TCP (選用組件) -->
      <dependency>
          <groupId>org.springframework.integration</groupId>
          <artifactId>spring-integration-ip</artifactId>
      </dependency>

      <!-- Spring Integration HTTP (選用組件) -->
      <dependency>
          <groupId>org.springframework.integration</groupId>
          <artifactId>spring-integration-http</artifactId>
      </dependency>
  ```

- Spring Integration 主要由三個組件組成：生產者(Producers)、訊息通道(Message Channel)、消費者(Consumers)。基本執行流程為 生產者 -> 訊息通道 -> 消費者

  - 生產者(Producers)

    1. 負責接收系統外部訊息，轉換生成內部訊息。
    2. 將訊息發送到訊息通道(Message Channel)。
    3. 可使用的介面例如:
       - HttpRequestHandlingMessagingGateway: 接受/回應 Http 訊息
       - FileReadingMessageSource: 處理系統上的檔案
       - MailReceivingMessageSource: 處理電子郵件

  - 訊息通道(Message Channel)

    1. 介於生產者與消費者之間，負責傳遞訊息
    2. 主要功能如下

       ```md
       - 解耦合
         Message Channel 提供了一種解耦生產者與消費者的方法。
         生產者不須知道訊息要發給誰，消費者也不須知道訊息來源。

       - 靈活性
         可在 Message Channel 中加入其他處理邏輯，如訊息過濾器、轉換器、分流器、路由器等等。
         無須改變生產者和消費者的程式碼，這提供了很大的靈活性。
         也可在此加入監控程式，以便測試。

       - 併發處理
         Message Channel 可配置為不同的類型，
         如 DirectChannel(單執行緒)、ExecutorChannel(多執行緒)、QueueChannel(帶隊列的通道)等，來處理不同的並發需求。

       - 重用性
         多個生產者可將訊息發送到同一 Message Channel，多個消費者也可從同一 Message Channel 接收訊息。
         此外，也可在 Message Channel 配置路由，或進行多個 Channel 組合，以實作更複雜的商業邏輯。
       ```

  - 消費者(Consumers)
    1. 負責從訊息通道接收訊息，並進行相應的處理
    2. 可使用 @ServiceActivator 調用系統內部方法，處理訊息。
    3. 可使用特定介面轉換訊息，回應給外部系統，如
       - HttpRequestHandlingMessagingGateway: 接受/回應 Http 訊息
       - FileWritingMessageHandler: 將訊息寫入檔案
       - MailSendingMessageHandler: 發送電子郵件

- Spring Integration 主要設計用於「建立由訊息驅動的企業應用程式」，主要功用是程式解耦、擴展、提高容錯。
  - 解耦: 生產者與消費者不須知道彼此的資訊
  - 擴展: 生產者不需等待消費者回應，故可處理更多的 reuqest；也可增加更多個消費者處理事件。
  - 容錯: 生產者與消費者透過 Channel 溝通，消費者當機時，Channel 可以暫存狀態，等待消費者回應。

## 常見生產者/消費者介面

- HTTP
  - HttpRequestHandlingMessagingGateway: 此介面同時能扮演生產者/消費者。
  - 用於處理 HTTP 請求並將其轉換為 Spring Integration 訊息。可以用來構建基於 HTTP 的 API。
- TCP 
  - TcpReceivingChannelAdapter: 用於接收 TCP 訊息並轉換為 Spring Integration 訊息。
  - TcpSendingMessageHandler: 用於發送 TCP 訊息。
- UDP
  - UdpInboundChannelAdapter: 用於接收 UDP 訊息並轉換為 Spring Integration 訊息。
  - UdpOutboundChannelAdapter: 用於發送 UDP 訊息。
- JMS
  - JmsInboundGateway: 用於接收 JMS 訊息並轉換為 Spring Integration 訊息。
  - JmsOutboundGateway: 用於發送 JMS 訊息。
  - Java Message Service(JMS): 由 Sun 與 MOM 廠商所共同制定的介面，定義了訊息的傳送、接收、頻道(Channel)、主題(Topic)、佇列 (Queue)等標準介面。
- AMQP
  - AmqpInboundGateway: 用於接收 AMQP 訊息並轉換為 Spring Integration 訊息，通常與 RabbitMQ 集成。
  - AmqpOutboundGateway: 用於發送 AMQP 訊息。
  - RabbitMQ: 開源的消息佇列系統，常用於分佈式系統中的異步通訊、任務調度、日誌收集、事件驅動架構等場景，主要使用 AMQP 協議溝通。
  - Advanced Message Queuing Protocol(AMQP): 高級訊息佇列協定，一種開放標準的應用層協定。
- File
  - FileReadingMessageSource: 用於監控系統目錄與讀取檔案。
  - FileWritingMessageHandler: 用於將訊息寫入檔案。
- Mail
  - MailReceivingMessageSource: 用於接收電子郵件並轉換為 Spring Integration 訊息。
  - MailSendingMessageHandler: 用於發送電子郵件。
- WebSocket
  - WebSocketInboundChannelAdapter: 用於接收 WebSocket 訊息並轉換為 Spring Integration 訊息。
  - WebSocketOutboundChannelAdapter: 用於發送 WebSocket 訊息。
- MQTT
  - MqttPahoMessageDrivenChannelAdapter: 用於接收 MQTT 訊息並轉換為 Spring Integration 訊息。
  - MqttPahoMessageHandler: 用於發送 MQTT 訊息。
  - Message Queuing Telemetry Transport(MQTT): 一種輕量級的訊息傳送協定，專為低頻寬、不穩定的網路所設計。常用於物聯網(IoT)設備間通訊。
- RMI
  - RmiInboundGateway: 用於接收 RMI 調用並轉換為 Spring Integration 訊息。
  - RmiOutboundGateway: 用於發送 RMI 調用。
  - Remote Method Invocation(RMI): 一種 Java 的機制，可使不同的 JVM 能透過網路調用彼此的方法。
- FTP
  - FtpInboundFileSynchronizer: 用於從 FTP server 同步檔案。
  - FtpOutboundGateway: 用於向 FTP server 傳送檔案。
- SFTP
  - SftpInboundFileSynchronizer: 用於從 SFTP server 同步檔案。
  - SftpOutboundGateway: 用於向 SFTP server 傳送檔案。


## 常見訊息通道介面、實作

### 介面
- MessageChannel
  - 基本 Message Channel Interface，用於傳遞訊息。這是所有 MessageChannel 的父介面。
  - 常見實作包括 DirectChannel、QueueChannel 等
- SubscribableChannel
  - 繼承 MessageChannel，允許多個消費者訂閱訊息。
  - 常見實作包括 PublishSubscribeChannel
- PollableChannel
  - 繼承 MessageChannel，支持輪詢訊息。
  - 常見實作包括 QueueChannel。

### 實作
- DirectChannel
    - 同步訊息通道，訊息在同一個執行緒中傳遞，點對點傳遞訊息。
    - 適用於低延遲、同步處理場景。
- QueueChannel
    - 基於佇列的訊息傳遞，訊息儲存在記憶體的佇列裡，可同步或異步接收訊息。
    - 適用於需要暫存訊息並進行異步處理的場景。
- PublishSubscribeChannel
    - 發布-訂閱模式，允許多個消費者訂閱同一個訊息通道，訊息會廣播給所有訂閱者。
    - 適用於需要將訊息傳遞給多個消費者的場景。
- PriorityChannel
    - 優先級的訊息傳遞，優先傳遞優先級高的訊息。
    - 適用於需要根據優先級處理訊息的場景。
- RendezvousChannel
    - 一種特殊的 QueueChannel，只有在消費者準備好接收訊息時，生產者才能發送訊息。
    - 適用於需要生產者和消費者同步的場景。
- ExecutorChannel
    - 使用 Spring 的 TaskExecutor 進行異步處理，訊息會在不同的執行緒中傳遞。
    - 適用於需要並行處理消息的場景。

