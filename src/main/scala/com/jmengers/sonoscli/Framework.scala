package com.jmengers.sonoscli

import com.jmengers.sonoscli.model._
import rx.lang.scala.Subject
import scala.concurrent.{Promise, Await, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Try

package object model {

  sealed trait State{
    def stateHistory: List[State]
  }

  case object BeginState extends State { def stateHistory = List.empty[State] }
  case class ArtistSelection(artists: List[String]) extends State { def stateHistory = List(BeginState) }
  case class AlbumSelection(albums: List[String], stateHistory: List[State]) extends State
  case class TrackSelection(tracks: List[String], stateHistory: List[State]) extends State

  type StateChange = State => Future[State]

  def representState: PartialFunction[State, Unit] = {
    case BeginState => beginStateRepresentation
    case a: ArtistSelection => artistSelectionRepresentation(a)
    case _ => ???
  }

  def searchArtists(input: String) = Framework.backable(input) { (query: String) =>
    Future.successful(ArtistSelection(List("Future Islands")))
  }


  val stateToHandler: PartialFunction[State, (String) => Option[() => Future[State]]] = {
    case BeginState => searchArtists
  }



  def beginStateRepresentation: Unit = {
    println("Search for an artist\n")
  }

  def artistSelectionRepresentation(state: ArtistSelection): Unit = {
    println("Select an artist\n")
  }
}


object Main {

  def main(args: Array[String]): Unit = {
    Await.result(Framework.start(model.stateToHandler), 30 minutes)
  }
}

object Framework {

  import scala.concurrent.ExecutionContext.Implicits.global

  case object Back

  val states = Subject[State]

  def backable(input: String)(fn: String => Future[State]): Option[() => Future[State]] = {
    input match {
      case "0" => None
      case _ => Some(() => fn(input) )
    }
  }

  def start(functions: PartialFunction[State, (String) => Option[() => Future[State]]]): Future[Unit] = {
    val p = Promise[Unit]
    states.subscribe { x =>

      representState(x)
      val input = StdIn.readLine

      functions(x)(input) match {
        case None => println("Terug")
        case Some(fn) => fn().map(s => states.onNext(s))
      }

      p.complete(Try(()))
    }

    states.onNext(BeginState)

    p.future
  }

}
