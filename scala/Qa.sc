import $ivy.`org.apache.spark::spark-sql:2.4.3`
import $ivy.`org.apache.spark::spark-streaming:2.4.3`
import $ivy.`org.apache.spark::spark-mllib:2.4.3`
import $ivy.`sh.almond::almond-spark:0.6.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.0`

import $ivy.`org.json4s::json4s-jackson:3.6.7`

import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.log4j.{Level, Logger}

import plotly._, plotly.element._, plotly.layout._, plotly.Almond._

object Html {
  import xml.Utility.escape
    def value(s: String) = escape(s).substring(0, 
        if(s.length > 10) 10 else s.length
    )
    
    def table(th: String, tb: String) = s"""
        <table>
        <thead style="font-weight: bold">$th</thead>
        <tbody>$tb</tbody>
        </table>
    """        

    def td(s: Seq[String]) = s.map( 
        f => s"<td>${value(f)}</td>"
    ).mkString

    def tr(ss: Seq[Seq[String]]) = ss.map( 
        s => s"<tr>${td(s)}</tr>"
    ).mkString
    
    def cols(ss: Seq[String]) = s"""
        <div style="column-count: 4"> ${ss.mkString("<br />")} </div>
    """
}


def setScope = catalyst.encoders.OuterScopes.addOuterScope _


def show[A](ds: Dataset[A]): Unit = ds match {
  case d : DataFrame => show(
    d.schema.fieldNames.toSeq, 
    d.take(5).map(_.toSeq)
  )

  case _ => show(
    ds.schema.fieldNames.toSeq, 
    ds.limit(5).toDF().take(5).map(_.toSeq)
  )
}

def show[A](hs: Seq[String], rs: Seq[Seq[A]]): Unit = publish.html(Html.table(
Html.td(hs),
Html.tr(rs.map { r => r.map(_.toString) })
))


def dir(o: Any): Unit = publish.html(Html.cols(
  o.getClass.getMethods.map(_.getName).filter( ! _.contains("$")).toSet.toSeq.sorted
))   





// if you want to have the plots available without an internet connection:
// init(offline=true)

// restrict the output height to avoid scrolling in output cells
//repl.pprinter() = repl.pprinter().copy(defaultHeight = 2)


Logger.getLogger("org").setLevel(Level.OFF)

val spark = SparkSession.builder()
    .master("local[*]")
    .getOrCreate()
    

import spark.implicits._