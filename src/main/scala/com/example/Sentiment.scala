package com.example

import java.io.File
import com.aliasi.util.Files
import com.aliasi.classify.{Classified, Classification, DynamicLMClassifier}

object Sentiment {//extends App {
  println("\nBASIC POLARITY DEMO")
  val mPolarityDir = new File("txt_sentoken")
  println("\nData Directory=" + mPolarityDir)
  val mCategories = mPolarityDir.list()
  val nGram = 8
  val mClassifier = DynamicLMClassifier.createNGramProcess(mCategories, nGram)
  val  positiveClassification = new Classification("positive")
  val  negativeClassification = new Classification("negative")
  train()
  evaluate()


  def isTrainingFile(file: File): Boolean = {
    file.getName().charAt(2) != '9'  // test on fold 9
  }

  def train() = {
    var numTrainingCases = 0
    var numTrainingChars = 0
    println("\nTraining.")
    try{
    for (i <- 0 until mCategories.length) {
      val category = mCategories(i)
      val  classification = new Classification(category)
      val file = new File(mPolarityDir,mCategories(i))
      val trainFiles = file.listFiles()
      for (j <- 0 until trainFiles.length) {
        val trainFile = trainFiles(j)
        if (isTrainingFile(trainFile)) {
          numTrainingCases += 1
          val review = Files.readFromFile(trainFile,"ISO-8859-1")
          numTrainingChars += review.length()
          val classified = new Classified[CharSequence](review,classification)
          mClassifier.handle(classified)
//          if (numTrainingCases % 100 == 0) evaluate()
        }
      }
    }
    }catch{
      case e :Exception =>   println("Error "+e.getMessage)
    }
    println("  # Training Cases=" + numTrainingCases);
    println("  # Training Chars=" + numTrainingChars);
  }

  def positiveText(text: String) = {
    val classified = new Classified[CharSequence](text ,positiveClassification)
    mClassifier.handle(classified)
  }

  def negativeText(text: String) = {
    val classified = new Classified[CharSequence](text ,negativeClassification)
    mClassifier.handle(classified)
  }

  def evaluate() = {
    println("\nEvaluating.")
    var numTests = 0;
    var numCorrect = 0;
    for (i <- 0 until mCategories.length) {
      val category = mCategories(i);
      val file = new File(mPolarityDir,mCategories(i))
      val trainFiles = file.listFiles()
      for (j <- 0 until trainFiles.length) {
        val trainFile = trainFiles(j)
        if (!isTrainingFile(trainFile)) {
          val review = Files.readFromFile(trainFile,"ISO-8859-1")
          numTests += 1
          val classification = classify(review)
          if (classification.bestCategory().equals(category))
            numCorrect += 1
        }
      }
    }
    println("  # Test Cases=" + numTests)
    println("  # Correct=" + numCorrect)
    println("  % Correct=" + numCorrect.asInstanceOf[Double]/numTests)
  }

  def classify(text: String) = {
    mClassifier.classify(text)
  }

}
