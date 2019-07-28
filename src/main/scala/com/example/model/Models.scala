package com.example.model

object Models {
  case class Point(x:Long, y:Long)
  case class CarRequest(from:Point, to:Point)
  case class WebCarRequest(source:Point,destination:Point) {
    require(source.x<=Int.MaxValue)
    require(source.x>=Int.MinValue)
    require(source.y<=Int.MaxValue)
    require(source.y>=Int.MinValue)
    def toCarRequest = CarRequest(source,destination)
  }
  case class BookResponse(car_id:Int, total_time:Long)
  case class GenericAPIResponse(status:String)
}