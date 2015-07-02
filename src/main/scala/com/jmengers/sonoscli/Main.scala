package com.jmengers.sonoscli

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import dispatch._, Defaults._
import net.liftweb.json._
import scala.io.StdIn

case class SpotifyResponse(artists: List[Artist])
case class Artist(href: String, name: String)
//case class Info(num_results: Int, limit: Int, offset: Int, query: String, type: String, )

sealed trait State
case object BeginState extends State
case class ArtistSelection(artists: List[Artist])  extends State
case class AlbumSelection(albums: List[String]) extends State
case class TrackSelection(tracks: List[String]) extends State
case object EndState extends State

object Main {
  implicit val formats = DefaultFormats

  def main(args: Array[String]): Unit = {
    val res = runProgram
    for {
    _ <- res
    } yield ()

    Await.ready(res, 30 minutes)
  }


  def runProgram: Future[Unit] = {
    beginStateToArtistSelection().map(x => ())
  }

  def beginStateToArtistSelection(): Future[ArtistSelection] = {
    val artistString = StdIn.readLine("Search for an artist:\n")

    val res = findArtists(artistString).map(ArtistSelection(_))

    res.map(x =>  x.artists.map(_.name).foreach(println) )

    res
  }

  def findArtists(query: String): Future[List[Artist]] = {

    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

    val req = url(s"http://ws.spotify.com/search/1/artist.json?q=$encodedQuery")

    val respString = Http(req OK as.String)

    respString.map { x =>
      val json = parse(x)
      val resp = json.extract[SpotifyResponse]
      resp.artists
    }
  }
}