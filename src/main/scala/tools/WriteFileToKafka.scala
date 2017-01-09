package tools


import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer._

object WriteFileToKafka extends App {

  import Support._

  //You'll need to supply arguments for the testDadtaFile, the topicName and the config's bootstrap.servers
  //The bootstrap servers need to be in the form "host1:port,host2:port"
  val testDataFile = args(0)

  val topicName = args(1)

  val config = ConfigFactory.parseString(s"""
  bootstrap.servers = "${args(2)}"
  client.id = testclient
  key.serializer=org.apache.kafka.common.serialization.StringSerializer
  value.serializer=org.apache.kafka.common.serialization.StringSerializer
  retry.backoff.ms = 10
""")

  val producer = kafkaProducer[String, String](toProperties(config))

  val data = readText(testDataFile).split("\n").map(_.trim)


  //Currently we write the same records over and over again.
  while(true) {

    data.foreach { record =>
      producer.send(new ProducerRecord(topicName, record), new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (exception != null)
            println(s"error writing data $record")
          else
            println(s"Kafka record written to topic/partition/offset ${metadata.topic}/${metadata.partition}/${metadata.offset}, data is $record")
        }
      })


    }
    Thread.sleep(1000)
  }



}
