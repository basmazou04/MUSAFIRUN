package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MinijocCapitalesFragment : Fragment() {
    private data class QuizQuestion(
        val questionRes: Int,
        val options: List<Int>,
        val correctOptionIndex: Int
    )

    private val questions = listOf(
        QuizQuestion(R.string.capital_question_france, listOf(R.string.capital_paris_world, R.string.capital_rome_world, R.string.capital_berlin_world), 0),
        QuizQuestion(R.string.capital_question_spain, listOf(R.string.capital_lisbon_world, R.string.capital_madrid_world, R.string.capital_athens_world), 1),
        QuizQuestion(R.string.capital_question_portugal, listOf(R.string.capital_lisbon_world, R.string.capital_vienna_world, R.string.capital_dublin_world), 0),
        QuizQuestion(R.string.capital_question_italy, listOf(R.string.capital_rome_world, R.string.capital_budapest_world, R.string.capital_prague_world), 0),
        QuizQuestion(R.string.capital_question_germany, listOf(R.string.capital_berlin_world, R.string.capital_warsaw_world, R.string.capital_oslo_world), 0),
        QuizQuestion(R.string.capital_question_japan, listOf(R.string.capital_beijing_world, R.string.capital_tokyo_world, R.string.capital_seoul_world), 1),
        QuizQuestion(R.string.capital_question_canada, listOf(R.string.capital_toronto_world, R.string.capital_ottawa_world, R.string.capital_montreal_world), 1),
        QuizQuestion(R.string.capital_question_australia, listOf(R.string.capital_canberra_world, R.string.capital_sydney_world, R.string.capital_melbourne_world), 0),
        QuizQuestion(R.string.capital_question_egypt, listOf(R.string.capital_cairo_world, R.string.capital_algiers_world, R.string.capital_tunis_world), 0),
        QuizQuestion(R.string.capital_question_brazil, listOf(R.string.capital_rio_world, R.string.capital_sao_paulo_world, R.string.capital_brasilia_world), 2)
    )

    private var currentIndex = 0
    private var score = 0
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var tvResult: TextView
    private lateinit var btnActionPrimary: Button
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_minijoc_capitales, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)
        tvProgress = view.findViewById(R.id.tvQuizProgress)
        tvQuestion = view.findViewById(R.id.tvQuizQuestion)
        btnOption1 = view.findViewById(R.id.btnOption1)
        btnOption2 = view.findViewById(R.id.btnOption2)
        btnOption3 = view.findViewById(R.id.btnOption3)
        tvResult = view.findViewById(R.id.tvQuizResult)
        btnActionPrimary = view.findViewById(R.id.btnPrimaryAction)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        btnActionPrimary.setOnClickListener { resetGame() }
        setupQuestion()
    }

    private fun setupQuestion() {
        if (currentIndex >= questions.size) {
            showResults()
            return
        }
        val question = questions[currentIndex]
        tvProgress.text = getString(R.string.quiz_progress, currentIndex + 1, questions.size)
        tvQuestion.text = getString(question.questionRes)
        val buttons = listOf(btnOption1, btnOption2, btnOption3)
        buttons.forEachIndexed { index, button ->
            button.text = getString(question.options[index])
            button.setOnClickListener { onAnswerSelected(index, question.correctOptionIndex) }
            button.visibility = View.VISIBLE
        }
        tvResult.visibility = View.GONE
        btnActionPrimary.visibility = View.GONE
    }

    private fun onAnswerSelected(selected: Int, correct: Int) {
        if (selected == correct) score++
        currentIndex++
        setupQuestion()
    }

    private fun showResults() {
        tvProgress.text = getString(R.string.quiz_completed)
        tvQuestion.text = getString(R.string.quiz_result_text, score, questions.size)
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        tvResult.visibility = View.VISIBLE
        tvResult.text = getString(R.string.quiz_result_text, score, questions.size)
        btnActionPrimary.visibility = View.VISIBLE
        btnActionPrimary.text = getString(R.string.quiz_play_again_or_exit)
    }

    private fun resetGame() {
        currentIndex = 0
        score = 0
        setupQuestion()
    }
}


