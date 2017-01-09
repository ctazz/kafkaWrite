package tools

import java.util.Properties

import com.fasterxml.jackson.databind.{SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.KafkaProducer

import scala.io.Source

object Support {

  import scala.collection.JavaConverters._

  def readText(fileName: String): String = {
    val source = Source.fromFile(fileName)
    try source.getLines().mkString("\n") finally source.close
  }


  def createObjectMapper(): ObjectMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
    m.registerModule(new JodaModule())
    m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    m
  }

  def toSimpleMap(config: Config): Map[String, String] = config.entrySet.asScala.foldLeft(Map.empty[String, String]) { (acc, configEntry) =>
    configEntry.getValue.unwrapped match {
      case value: String => acc + (configEntry.getKey -> value)
      case _ => acc
    }
  }

  //Yes, this is slower than directly creating Properties from Config, but it's not going to be called very often, is it?
  def toProperties(config: Config): Properties = toSimpleMap(config).foldLeft(new Properties) { (props, pair) => props.put(pair._1, pair._2); props }

  def kafkaProducer[K,V](properties: Properties):  KafkaProducer[K,V] = new KafkaProducer(properties)
}