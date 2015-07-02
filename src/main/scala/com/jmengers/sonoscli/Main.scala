package com.jmengers.sonoscli

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import dispatch._, Defaults._

object Main {
  def main(args: Array[String]): Unit = {
    val res = findArtists("Future Islands")
    for {
      artists <- res
    } yield println(artists)

    Await.ready(res, 10 minutes)

  }

  def findArtists(query: String): Future[String] = {

    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

    val req = url(s"http://ws.spotify.com/search/1/artist.json?q=$encodedQuery")

    Http(req OK as.String)
  }
}