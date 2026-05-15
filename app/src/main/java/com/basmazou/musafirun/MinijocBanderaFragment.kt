package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class MinijocBanderaFragment : Fragment() {
    private data class FlagQuestion(
        val flagResId: Int,
        val options: List<Int>,
        val correctOptionIndex: Int
    )

    private val questions = listOf(
        FlagQuestion(R.drawable.minijuego1nepal, listOf(R.string.country_nepal, R.string.country_bhutan, R.string.country_libya), 0),
        FlagQuestion(R.drawable.minijuego2bhutan, listOf(R.string.country_bhutan, R.string.country_belize, R.string.country_cyprus), 0),
        FlagQuestion(R.drawable.minijuego3mozambique, listOf(R.string.country_mozambique, R.string.country_kazakhstan, R.string.country_switzerland), 0),
        FlagQuestion(R.drawable.minijuego4switzerland, listOf(R.string.country_switzerland, R.string.country_vatican_city, R.string.country_nepal), 0),
        FlagQuestion(R.drawable.minijuego5vaticano, listOf(R.string.country_vatican_city, R.string.country_papua_new_guinea, R.string.country_mozambique), 0),
        FlagQuestion(R.drawable.minijuego6papuanewguinea, listOf(R.string.country_papua_new_guinea, R.string.country_kazakhstan, R.string.country_libya), 0),
        FlagQuestion(R.drawable.minijuego7kazakhstan, listOf(R.string.country_kazakhstan, R.string.country_nepal, R.string.country_bhutan), 0),
        FlagQuestion(R.drawable.minijuego8belize, listOf(R.string.country_belize, R.string.country_cyprus, R.string.country_switzerland), 0),
        FlagQuestion(R.drawable.minijuego9cyprus, listOf(R.string.country_cyprus, R.string.country_belize, R.string.country_vatican_city), 0),
        FlagQuestion(R.drawable.minijuego10libya, listOf(R.string.country_libya, R.string.country_mozambique, R.string.country_papua_new_guinea), 0)
    )
    private var currentIndex = 0
    private var score = 0

    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var ivFlag: ImageView
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
        return inflater.inflate(R.layout.fragment_minijoc_bandera, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)
        tvProgress = view.findViewById(R.id.tvQuizProgress)
        tvQuestion = view.findViewById(R.id.tvQuizQuestion)
        ivFlag = view.findViewById(R.id.ivFlag)
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
        tvQuestion.text = getString(R.string.minijuego_banderas_pregunta)
        ivFlag.setImageResource(question.flagResId)

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
        ivFlag.visibility = View.GONE
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        tvResult.visibility = View.VISIBLE
        tvResult.text = getString(R.string.quiz_result_text, score, questions.size)
        btnActionPrimary.visibility = View.VISIBLE
        btnActionPrimary.text = getString(R.string.quiz_play_again)
    }

    private fun resetGame() {
        currentIndex = 0
        score = 0
        ivFlag.visibility = View.VISIBLE
        setupQuestion()
    }
}


