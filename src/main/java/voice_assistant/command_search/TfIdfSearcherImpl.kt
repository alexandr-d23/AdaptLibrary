package voice_assistant.command_search

import android.content.Context
import android.util.Log
import voice_assistant.Utils
import com.github.demidko.aot.WordformMeaning
import java.lang.StringBuilder
import java.util.Collections
import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

class TfIdfSearcherImpl(context: Context) : CommandSearcher {

    private val enExclude = context.applicationContext.assets.open("en").bufferedReader().use {
        it.readText()
    }
    private val ruExclude = context.applicationContext.assets.open("ru").bufferedReader().use {
        it.readText()
    }

    override fun searchCommand(commands: List<String>, voiceCommand: String): Int {
        try {
            val availableCommandsLemmas = mutableListOf<List<String>>()
            commands.forEachIndexed { index, s ->
                val commandTokens = getTokensFromText(s)
                availableCommandsLemmas.add(commandTokens.map { token ->
                    token
                })
                Log.d("TfIdf", "CommandTokens: $commandTokens, lemmas: $availableCommandsLemmas")
            }
            val userCommandLemmas = getTokensFromText(voiceCommand)
            val tfIdf = getWordsTfIdf(availableCommandsLemmas)

            return getCosSimilarity(commands, tfIdf, userCommandLemmas)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e("TfIdf", "Error: $e")
            return -1
        }
    }

    private fun getLemmaForWord(word: String): String {
        return try {
            WordformMeaning.lookupForMeanings(word)[0].lemma.toString().also {
                Log.d("TfIdf", "Token '$word', lemma: $it")
            }
        } catch (e: Throwable) {
            return word
        }
    }

    private fun getTokensFromText(text: String): List<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^А-Яа-яA-Za-z ]"), " ")
            .split(" ")
            .filter {
                !enExclude.contains(it) &&
                        !ruExclude.contains(it) &&
                        !it.contains(Regex("[0-9]+")) &&
                        it.isNotEmpty()
            }
            .distinct()
    }

    private fun getWordsTfIdf(
        documents: List<List<String>>,
    ): List<Map<String, Double>> {
        val tf = getWordsTf(documents)
        val invertedWords = getInverted(documents)
        val idf = getWordsIdf(documents, invertedWords)
        val tfIdf = mutableListOf<Map<String, Double>>()
        tf.forEachIndexed { documentIndex, wordsTf ->
            val wordsToTfIdf = mutableMapOf<String, Double>()
            wordsTf.forEach { tf ->
                wordsToTfIdf[tf.key] = tf.value * idf[documentIndex][tf.key]!!
            }
            tfIdf.add(wordsToTfIdf)
        }
        return tfIdf
    }

    private fun getWordsTf(documents: List<List<String>>): List<Map<String, Double>> {
        return documents.map { tokens ->
            val tf = mutableMapOf<String, Double>()
            tokens.forEach { termin ->
                tf[termin]?.let {
                    tf[termin] = it + (1.0 / tokens.size)
                } ?: run {
                    tf[termin] = 1.0 / tokens.size
                }
            }
            tf
        }
    }

    private fun getWordsIdf(
        documents: List<List<String>>,
        invertedWords: Map<String, Set<Int>>,
    ): List<Map<String, Double>> {
        return documents.map { tokens ->
            val result = tokens.map {
                val idfWithoutLn = documents.size.toDouble() / invertedWords[it]!!.size
                var idf = ln(idfWithoutLn)
                if (invertedWords.size == 1) idf = 1.0
                Pair(it, idf)
            }
            mutableMapOf<String, Double>().apply {
                putAll(result)
            }
        }
    }

    private fun getInverted(documents: List<List<String>>): Map<String, Set<Int>> {
        val wordsWithIndex = mutableMapOf<String, MutableSet<Int>>()
        documents.forEachIndexed { i, page ->
            page.forEach {
                val word = it
                wordsWithIndex[word]?.add(i) ?: run {
                    wordsWithIndex[word] = mutableSetOf(i)
                }
            }
        }
        return wordsWithIndex
    }

    private fun getCosSimilarity(
        commands: List<String>,
        documentsTfIdf: List<Map<String, Double>>,
        userCommandLemmas: List<String>
    ): Int {
        val lemmasSet = mutableSetOf<String>()
        documentsTfIdf.forEach { entries ->
            entries.forEach {
                lemmasSet.add(it.key)
            }
        }
        val lemmaToListIndex = hashMapOf<String, Int>().apply {
            putAll(
                lemmasSet.mapIndexed { index, s ->
                    Pair(s, index)
                }
            )
        }

        val matrix = documentsTfIdf.map {
            val list = ArrayList<Double>(Collections.nCopies(lemmasSet.size, 0.0))
            it.forEach { (lemma, tfId) ->
                list[lemmaToListIndex[lemma]!!] = tfId
            }
            list
        }
        val commandsLog = StringBuilder("                                  ")
        lemmasSet.forEach {
            commandsLog.append(String.format("%-10s |", it))
        }
        Log.d("TfIdfSearch", commandsLog.toString())
        matrix.forEachIndexed { ind, array ->
            val logBuild = StringBuilder(String.format("| %-30s |", "Команда: ${commands[ind]}"))
            array.map { d ->
                logBuild.append(String.format("%-10f |", d))
            }
            Log.d("TfIdfSearch", logBuild.toString())
        }
        val searchVector = ArrayList<Double>(Collections.nCopies(lemmasSet.size, 0.0))
        lemmasSet.forEach { lemma ->
            var minDistance = userCommandLemmas.map { userLemma ->
                Utils.levenshtein(lemma, userLemma)
            }.min()
            if (minDistance == 0.0) minDistance = 1.0
            searchVector[lemmaToListIndex[lemma]!!] = 1.0 / minDistance
        }

        val cosinSimilarity = matrix.mapIndexed { docIndex, vector ->
            val product = vector.mapIndexed { index, d ->
                d * searchVector[index]
            }.sum()
            val lengthVectorPage = sqrt(vector.sumOf {
                it * it
            })
            val lengthVectorSearch = sqrt(searchVector.sumOf {
                it * it
            })
            val digitResult = product / (lengthVectorSearch * lengthVectorPage)
            Log.d(
                "TfIdfSearch",
                "VectorPage: $lengthVectorPage  VectorSearch $lengthVectorSearch Product $product $digitResult"
            )
            Log.d(
                "TfIdfSearch",
                "Команда: '${commands[docIndex]}' Результат косинусного сходства: $digitResult "
            )
            digitResult
        }
        val similarity = cosinSimilarity.max()
        val min = cosinSimilarity.min()
        return if (similarity > 0.65) cosinSimilarity.indexOf(cosinSimilarity.max())
        else -1
    }
}