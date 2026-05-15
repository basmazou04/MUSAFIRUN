package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MinijocCulturaGeneralFragment : Fragment() {
    private data class QuizQuestion(
        val questionRes: Int,
        val options: List<Int>,
        val correctOptionIndex: Int
    )

    private val questions = listOf(
        QuizQuestion(R.string.gk_question_1, listOf(R.string.gk_q1_opt1, R.string.gk_q1_opt2, R.string.gk_q1_opt3), 0),
        QuizQuestion(R.string.gk_question_2, listOf(R.string.gk_q2_opt1, R.string.gk_q2_opt2, R.string.gk_q2_opt3), 1),
        QuizQuestion(R.string.gk_question_3, listOf(R.string.gk_q3_opt1, R.string.gk_q3_opt2, R.string.gk_q3_opt3), 2),
        QuizQuestion(R.string.gk_question_4, listOf(R.string.gk_q4_opt1, R.string.gk_q4_opt2, R.string.gk_q4_opt3), 0),
        QuizQuestion(R.string.gk_question_5, listOf(R.string.gk_q5_opt1, R.string.gk_q5_opt2, R.string.gk_q5_opt3), 2),
        QuizQuestion(R.string.gk_question_6, listOf(R.string.gk_q6_opt1, R.string.gk_q6_opt2, R.string.gk_q6_opt3), 1),
        QuizQuestion(R.string.gk_question_7, listOf(R.string.gk_q7_opt1, R.string.gk_q7_opt2, R.string.gk_q7_opt3), 0),
        QuizQuestion(R.string.gk_question_8, listOf(R.string.gk_q8_opt1, R.string.gk_q8_opt2, R.string.gk_q8_opt3), 2),
        QuizQuestion(R.string.gk_question_9, listOf(R.string.gk_q9_opt1, R.string.gk_q9_opt2, R.string.gk_q9_opt3), 1),
        QuizQuestion(R.string.gk_question_10, listOf(R.string.gk_q10_opt1, R.string.gk_q10_opt2, R.string.gk_q10_opt3), 0)
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
        return inflater.inflate(R.layout.fragment_minijoc_cultura_general, container, false)
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


