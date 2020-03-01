package com.example.yhaa37

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import com.github.florent37.viewanimator.ViewAnimator
import kotlinx.android.synthetic.main.helper_view_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.concurrent.TimeUnit

class ButtonSpace(val context: Context) : View.OnClickListener {

    private val activity = context as Activity
    private val pref = GetAndStoreData(context)
    private val animationInAction = AnimationInAction(context)
    private val arrangeScreen = ArrangeScreen(context)
    val helper = Helper(context)

    private var statrTime: Long = 0
    private var endTime = System.nanoTime()

    fun drawAnim() {
        var showPosition = pref.getShowPosition()
        if (!showPosition) {
            updateTitleTalkerSituation()
        }
        // view.tvAnimatinKind.text = text
        val cu = getCurrentPage()
        activity.tvPage.text = cu.toString()
        pref.currentTalk().numTalker = cu
        //   updateTitleTalkerSituation()
        animationInAction.executeTalker()
    }

    fun letsPlay(v: View) {

        when (v.id) {
            R.id.textRevBtn -> readAgainTextFile()
            R.id.newPageBtn -> enterNewPage()
            R.id.showPositionBtn -> changeShowPosition()
            R.id.toShowModeBtn -> animationInAction.executeTalker()
            R.id.plusAndMinusBtn -> changePlusMinusMode()
            R.id.saveButton -> saveIt()
            R.id.nextButton -> nextIt()
            R.id.previousButton -> previousIt()
            R.id.lastTalker_button -> retriveLastTalker()
            R.id.reSizeTextBtn -> minTextSize()
            R.id.fab -> nextIt()
            R.id.fab1 -> previousIt()
            else -> drawAnim()

        }

    }

    private fun changeShowPosition() {
        var showPosition = pref.getShowPosition()
        showPosition = !showPosition
        pref.saveShowPosition(showPosition)
        setShowPositionMode()
        if (showPosition) activity.showPositionBtn.text = "toTest"
        else activity.showPositionBtn.text = "toShow"
    }

    fun setShowPositionMode() {
        var showPosition = pref.getShowPosition()

        with(activity) {
            if (!showPosition) {
                arrangeScreen.setLayoutShowMode()
                arrangeScreen.operateListView()


                plusAndMinusBtn.text = "+"
                lastTalker_button.text = "Last"
                saveButton.text = "Save"
                upper_layout.visibility = VISIBLE
                down_layout.visibility = VISIBLE

                textRevBtn.visibility = VISIBLE
                reSizeTextBtn.visibility = VISIBLE
                newPageBtn.visibility = VISIBLE
                toShowModeBtn.visibility = VISIBLE

                style_ListView.visibility = VISIBLE
                para_ListView.visibility = VISIBLE
                ttPara_listView.visibility = VISIBLE
                action_ListView.visibility = VISIBLE
                tvAnimatinKind.visibility = VISIBLE
                tvPage.visibility = VISIBLE
                fab.hide()
                fab1.hide()
            }

            if (showPosition) {
                helper.activateHowSpeaking()
                down_layout.visibility = INVISIBLE
                upper_layout.visibility = VISIBLE
                textRevBtn.visibility = INVISIBLE
                reSizeTextBtn.visibility = INVISIBLE
                newPageBtn.visibility = INVISIBLE
                toShowModeBtn.visibility = INVISIBLE

                style_ListView.visibility = INVISIBLE
                para_ListView.visibility = INVISIBLE
                ttPara_listView.visibility = INVISIBLE
                action_ListView.visibility = INVISIBLE
                tvAnimatinKind.visibility = INVISIBLE
                tvPage.visibility = VISIBLE
                fab.show()
                fab1.show()
            }
        }
    }

    private fun readAgainTextFile() {
        var list = ArrayList<Talker>()

        CoroutineScope(Dispatchers.IO).launch {
            val talkList = async { pref.getTalkingList(1) }
            val textTalkList = async { pref.createTalkListFromTheStart() }
            val talkList1 = async { textReRead(talkList.await(), textTalkList.await()) }
            pref.saveTalkingList(talkList1.await())
            list = talkList1.await()
            withContext(Dispatchers.Main) {
                delay(1000)
                drawAnim()
            }
        }
    }


    fun textReRead(
        talkList: ArrayList<Talker>,
        textTalkList: ArrayList<Talker>
    ): ArrayList<Talker> {
        for (index in 0..talkList.size - 1) {
            val st1 = textTalkList[index].taking
            var arr = st1.split("\n")
            val ar = arrayListOf<String>()
            for (item in arr) {
                if (item != "") {
                    ar.add(item)
                }
            }

            if (index > talkList.size) {
                var talk1 = textTalkList[index].copy()
                talkList.add(talk1)

            } else {

                talkList[index].takingArray = ar
                talkList[index].taking = textTalkList[index].taking
            }

            if (index == textTalkList.size - 1) {
                talkList.dropLast(talkList.size - textTalkList.size)
                return talkList
            }
        }
        return talkList
    }


    private fun minTextSize() {
        updateLastTalker(0)
        val list = pref.getTalkingList(1)
        val index = pref.getCurrentPage()
        list[index].textSize = 12f  // for accsident of bigest text
        pref.saveTalkingList(list)
        drawAnim()
    }

    private fun enterNewPage() {

        var myDialog = AlertDialog.Builder(context)

        val input = EditText(context)
        myDialog.setView(input)
        myDialog.setTitle("Enter new page")
        myDialog.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val num = input.text.toString().toInt()
                pref.saveCurrentPage(num)
                drawAnim()
                return
            }

        })
        myDialog.setNegativeButton("CANCEL", null)
        myDialog.show()
    }

    private fun retriveLastTalker() {
        updateLastTalker(1)
        drawAnim()
    }

    private fun updateLastTalker(ind: Int) {
        val talkList = pref.getTalkingList(1)
        with(pref) {
            if (ind == 0) {
                // val sa = pref.currentTalk()
                saveLastTalker(currentTalk())
            } else {
                talkList[this@ButtonSpace.getCurrentPage()] = getLastTalker().copy()
            }
        }
    }

    fun saveIt() {
        val talkList = pref.getTalkingList(1)

        pref.saveTalkingList(talkList)
        Toast.makeText(context, "It's save Mr", Toast.LENGTH_SHORT).show()
    }

    fun updateTitleTalkerSituation() {
        with(pref.currentTalk()) {
            val text =
                "l=${takingArray.size}sty=$styleNum anim=$animNum size=${textSize.toInt()}" +
                        " bord=$borderWidth dur=$dur sw=$swingRepeat"
            activity.tvAnimatinKind.text = text
        }
        activity.tvPage.text = pref.getCurrentPage().toString()
    }

    fun previousIt() {
        pref.saveLastTalker(pref.currentTalk())
        var cu = getCurrentPage()
        cu--
        pref.saveCurrentPage(cu)
        nextAndPriviousAction()
        /*drawAnim()
        CoroutineScope(Main).launch {
            waitTillAnimationEnd()

        }*/
    }

    fun nextIt() {
        pref.saveLastTalker(pref.currentTalk())
        var cu = getCurrentPage()
        cu++
        pref.saveCurrentPage(cu)
        nextAndPriviousAction()
        /*drawAnim()
        CoroutineScope(Main).launch {
            waitTillAnimationEnd()
        }*/
    }

    private fun nextAndPriviousAction(){

            animationInActionSign(1, 500)
            activity.fab.isClickable = false
            activity.fab1.isClickable = false
            drawAnim()
            val talker = pref.currentTalk()
        CoroutineScope(Main).launch {
            //delay(talker.dur + 200)
            delay(talker.dur)
            activity.fab.isClickable = true
            activity.fab1.isClickable = true
            animationInActionSign(0, 500)
        }

    }

   /* private suspend fun waitTillAnimationEnd() {
        val talker = pref.currentTalk()
        delay(talker.dur + 200)
        activity.fab.isClickable = true
        activity.fab1.isClickable = true
        animationInActionSign(0, 500)
         withContext(Main) {
             animationInActionSign(0, 600)
         }
    }*/

    fun animationInActionSign(ind: Int, dur: Long) {
        if (ind == 0) {
            ViewAnimator
                .animate(activity.tvPage)
                .backgroundColor(Color.RED, Color.GREEN)
                .duration(dur)
                .start()

        } else {
            ViewAnimator
                .animate(activity.tvPage)
                .backgroundColor(Color.GREEN, Color.RED)
                .duration(dur)
                .start()
        }
    }

    private fun changePlusMinusMode() {
        with(activity.plusAndMinusBtn) {
            if (text == "+") {
                text = "-"
            } else {
                text = "+"
            }
        }
    }


    private fun time(st: String) {
        endTime = System.nanoTime()
        val interval = TimeUnit.MILLISECONDS.convert(endTime - statrTime, TimeUnit.NANOSECONDS)
        Log.d("clima", st + " --> $interval ms")

    }

    override fun onClick(view: View) {

        letsPlay(view)


    }

/* private fun onClickOther(view: View) {
     val talkList = pref.getTalkingList(1)

     var def = 0
     if (view == activity.fab) {
         def++
     }
     if (view == activity.fab1) {
         def--
     }

     var counterStep = getCurrentPage() + def

     if (counterStep < 1) counterStep = 1
     if (counterStep == talkList.size) counterStep = 1
     pref.saveCurrentPage(counterStep)

     if (showPosition) {
         time("onClickA113")
         buttonActivation(0)

     }

     chageBackgroundColor(1, 1000)

     letsPlay(view)

     val size = pref.getLastTalker().takingArray.size
      Utile.listener1 = { it1, _ ->
         // Log.d("clima", "Hii num->$it1 and time->$it2 and size=$size")
         if (size == 1 || it1 == size) {
             time("onClickB114")
             buttonActivation(1)
             chageBackgroundColor(0, 1000)
         }
     }
 }*/

    fun getCurrentPage(): Int {
        val talkList = pref.getTalkingList(1)

        var cu = pref.getCurrentPage()
        if (cu < 1 || cu >= talkList.size) {
            cu = 1
            pref.saveCurrentPage(cu)
        }
        return cu
    }

    fun chageBackgroundColor(ind: Int, dur: Long) {
        if (ind == 0) {
            ViewAnimator
                .animate(activity.tvPage)
                .backgroundColor(Color.RED, Color.GREEN)
                .duration(dur)
                .start()

        } else {
            ViewAnimator
                .animate(activity.tvPage)
                .backgroundColor(Color.GREEN, Color.RED)
                .duration(dur)
                .start()
        }
    }

    fun fabAnimation(ind: Int) {
        if (ind == 0) {
            ViewAnimator
                .animate(activity.fab)
                .alpha(0f)
                .andAnimate(activity.fab1)
                .alpha(0f)
                .duration(2000)
                .start()
        } else {
            ViewAnimator
                .animate(activity.fab)
                .alpha(0f, 1f)
                .andAnimate(activity.fab1)
                .alpha(0f, 1f)
                .duration(2000)
                .start()
        }
    }


    fun buttonActivation(ind: Int) {
        time("buttonActivation 1 ind=$ind")
        var showPosition = pref.getShowPosition()


        with(activity) {
            if (ind == 0) {
                if (showPosition) {
                    fab.isClickable = false
                    fab1.isClickable = false
                    fabAnimation(0)
                } else {
                    textRevBtn.visibility = INVISIBLE
                    newPageBtn.visibility = INVISIBLE
                    toShowModeBtn.visibility = INVISIBLE
                    plusAndMinusBtn.visibility = INVISIBLE
                    showPositionBtn.visibility = INVISIBLE
                    saveButton.visibility = INVISIBLE
                    nextButton.visibility = INVISIBLE
                    previousButton.visibility = INVISIBLE
                    lastTalker_button.visibility = INVISIBLE
                    reSizeTextBtn.visibility = INVISIBLE
                }
            }
            if (ind == 1) {
                if (showPosition) {
                    fab.isClickable = true
                    fab1.isClickable = true
                    fabAnimation(1)
                } else {
                    textRevBtn.visibility = VISIBLE
                    newPageBtn.visibility = VISIBLE
                    toShowModeBtn.visibility = VISIBLE
                    plusAndMinusBtn.visibility = VISIBLE
                    showPositionBtn.visibility = VISIBLE
                    saveButton.visibility = VISIBLE
                    nextButton.visibility = VISIBLE
                    previousButton.visibility = VISIBLE
                    lastTalker_button.visibility = VISIBLE
                    reSizeTextBtn.visibility = VISIBLE
                }
            }
        }
        time("buttonActivation 2 ind=$ind")
    }

    fun initButton() {
        with(activity) {
            showPositionBtn.setOnClickListener { onClick(showPositionBtn) }
            textRevBtn.setOnClickListener { onClick(textRevBtn) }
            newPageBtn.setOnClickListener { onClick(newPageBtn) }
            plusAndMinusBtn.setOnClickListener { onClick(plusAndMinusBtn) }
            saveButton.setOnClickListener { onClick(saveButton) }
            nextButton.setOnClickListener { onClick(nextButton) }
            previousButton.setOnClickListener { onClick(previousButton) }
            lastTalker_button.setOnClickListener { onClick(lastTalker_button) }
            reSizeTextBtn.setOnClickListener { onClick(reSizeTextBtn) }
            toShowModeBtn.setOnClickListener { onClick(toShowModeBtn) }
            fab.setOnClickListener { onClick(fab) }
            fab1.setOnClickListener { onClick(fab1) }
        }
        setShowPositionMode()
    }
}