package jb

import jb.util.functions.WeightAggregators
import org.scalatest.FunSuite

class MultiRunnerTest extends FunSuite {

  test("shouldComposeProperHeader") {
    val expectedHeader = Array(
      "MV(ACC)",
      "MV(MCC)",
      "RF(ACC)",
      "RF(MCC)",
      "wMV^inv(ACC)",
      "wMV^inv(MCC)",
      "Psi_1^inv(ACC)",
      "Psi_1^inv(MCC)",
      "Psi_3^inv(ACC)",
      "Psi_3^inv(MCC)",
      "Psi_2^inv(ACC)",
      "Psi_2^inv(MCC)",
      "wMV^vol(ACC)",
      "wMV^vol(MCC)",
      "Psi_1^vol(ACC)",
      "Psi_1^vol(MCC)",
      "Psi_3^vol(ACC)",
      "Psi_3^vol(MCC)",
      "Psi_2^vol(ACC)",
      "Psi_2^vol(MCC)"
    )
    val header = MultiRunner.composeHeader(
      Array(
        WeightAggregators.sumOfVolumesInv,
        WeightAggregators.sumOfVolumes
      ),
      Array(1, 3, 2)
    )
    assert(header.length == expectedHeader.length)
    header.indices.foreach(index => assert(header(index) == expectedHeader(index)))
  }

}
