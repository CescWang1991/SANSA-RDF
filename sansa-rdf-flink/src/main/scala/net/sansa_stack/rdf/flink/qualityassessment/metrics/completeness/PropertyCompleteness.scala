package net.sansa_stack.rdf.flink.qualityassessment.metrics.completeness

import org.apache.jena.graph.{ Triple, Node }
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala.DataSet
import org.apache.flink.api.scala._
import net.sansa_stack.rdf.flink.qualityassessment.dataset.DatasetUtils
import net.sansa_stack.rdf.flink.data.RDFGraph

/**
 * This metric measures the property completeness by checking
 * the missing object values for the given predicate and given class of subjects.
 * A user specifies the RDF class and the RDF predicate, then it checks for each pair
 * whether instances of the given RDF class contain the specified RDF predicate.
 */
object PropertyCompleteness {

  @transient var env: ExecutionEnvironment = _

  val subject = DatasetUtils.getSubjectClassURI()
  val property = DatasetUtils.getPropertyURI()

  def apply(rdfgraph: RDFGraph): Long = {

    /*
     -->Rule->Filter-->
   		"select (?s) where ?s p=rdf:type o=Type; p2=Property ?o2
			select (?s2) where ?s2 p=rdf:type o=Type"
			-->Action-->
			S+=?s && S2+=?s2
			-->Post-processing-->
			|S| / |S2|
   */
    val dataset = rdfgraph.triples
    val s2 = dataset.filter(f =>
      f.getPredicate.getLiteralLexicalForm.contains("rdf:type")
        && f.getObject.getLiteralLexicalForm.contains(subject))

    val s = s2.filter(_.getPredicate.getLiteralLexicalForm.contains(property))

    val S = s.map(_.getSubject).distinct().count()
    val S2 = s2.map(_.getSubject).distinct().count()

    if (S2 > 0) S / S2
    else 0
  }

}