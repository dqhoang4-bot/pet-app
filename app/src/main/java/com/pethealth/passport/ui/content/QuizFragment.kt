package com.pethealth.passport.ui.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.radiobutton.MaterialRadioButton
import com.pethealth.passport.R
import com.pethealth.passport.data.repository.QuizQuestion
import com.pethealth.passport.databinding.FragmentQuizBinding
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.visible

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val english: Boolean by lazy { container.contentRepository.isEnglish() }
    private val questions: List<QuizQuestion> by lazy { container.contentRepository.quizQuestions }

    private var index = 0
    private var score = 0
    private var answered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtIntro.text = getString(R.string.quiz_intro, questions.size)
        binding.btnStart.setOnClickListener { start() }
        binding.btnRestart.setOnClickListener { restart() }
        binding.btnNext.setOnClickListener { advance() }
        binding.radioOptions.setOnCheckedChangeListener { group, checkedId ->
            if (answered || checkedId == RadioGroup.NO_ID) return@setOnCheckedChangeListener
            val option = group.findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            evaluate(option.tag as Int)
        }
    }

    private fun start() {
        index = 0
        score = 0
        binding.introGroup.visible(false)
        binding.resultGroup.visible(false)
        binding.questionGroup.visible(true)
        renderQuestion()
    }

    private fun restart() {
        index = 0
        score = 0
        answered = false
        binding.questionGroup.visible(false)
        binding.resultGroup.visible(false)
        binding.introGroup.visible(true)
    }

    private fun renderQuestion() {
        val question = questions[index]
        answered = false
        binding.txtProgress.text = getString(
            R.string.quiz_question_of,
            index + 1,
            questions.size
        )
        binding.txtQuestion.text = question.question(english)
        binding.txtFeedback.visible(false)
        binding.btnNext.isEnabled = false
        binding.btnNext.text = getString(
            if (index == questions.lastIndex) R.string.quiz_finish else R.string.quiz_next
        )

        binding.radioOptions.removeAllViews()
        question.options(english).forEachIndexed { optionIndex, option ->
            val button = MaterialRadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = option
                tag = optionIndex
                setPadding(0, 10, 0, 10)
            }
            binding.radioOptions.addView(
                button,
                RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun evaluate(selected: Int) {
        val question = questions[index]
        val correct = selected == question.correctIndex
        answered = true
        if (correct) score++

        for (i in 0 until binding.radioOptions.childCount) {
            binding.radioOptions.getChildAt(i).isEnabled = false
        }

        val correctAnswer = question.options(english)[question.correctIndex]
        val feedback = buildString {
            append(getString(if (correct) R.string.quiz_correct else R.string.quiz_wrong))
            if (!correct) {
                append(" — ")
                append(correctAnswer)
            }
            append("\n\n")
            append(getString(R.string.quiz_explanation))
            append(": ")
            append(question.explanation(english))
        }
        binding.txtFeedback.text = feedback
        binding.txtFeedback.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (correct) R.color.brand_teal_light else R.color.status_overdue_bg
        )
        binding.txtFeedback.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (correct) R.color.brand_teal_dark else R.color.status_overdue
            )
        )
        binding.txtFeedback.visible(true)
        binding.btnNext.isEnabled = true
    }

    private fun advance() {
        if (index < questions.lastIndex) {
            index++
            renderQuestion()
        } else {
            showResult()
        }
    }

    private fun showResult() {
        binding.questionGroup.visible(false)
        binding.resultGroup.visible(true)
        binding.txtScore.text = getString(R.string.quiz_score, score, questions.size)
        val ratio = score.toFloat() / questions.size
        binding.txtResult.text = getString(
            when {
                ratio >= 0.8f -> R.string.quiz_result_excellent
                ratio >= 0.5f -> R.string.quiz_result_good
                else -> R.string.quiz_result_low
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
