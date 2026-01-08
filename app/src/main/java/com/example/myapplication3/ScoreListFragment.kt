package com.example.myapplication3

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScoreListFragment : Fragment() {

    // ממשק (Interface) לתקשורת עם האקטיביטי
    interface CallBack_List {
        fun onRowClick(lat: Double, lon: Double)
    }

    private var callBackList: CallBack_List? = null

    // פונקציה שהאקטיביטי יקרא לה כדי להירשם לאירועים
    fun setCallBackList(callBack: CallBack_List) {
        this.callBackList = callBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val listView = ListView(context)
        listView.layoutDirection = View.LAYOUT_DIRECTION_LTR

        val top10List = ScoreManager.getTop10Scores(requireContext())
        val adapter = ScoreAdapter(requireContext(), top10List)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = top10List[position]
            callBackList?.onRowClick(item.lat, item.lon)
        }

        return listView
    }

    class ScoreAdapter(context: Context, private val dataSource: List<ScoreItem>) :
        ArrayAdapter<ScoreItem>(context, R.layout.item_score, dataSource) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_score, parent, false)
            val item = dataSource[position]

            val lblName = rowView.findViewById<TextView>(R.id.lblRankAndName)
            val lblScore = rowView.findViewById<TextView>(R.id.lblScore)
            val lblType = rowView.findViewById<TextView>(R.id.lblType)

            lblName.text = "${position + 1}. ${item.name}"
            lblScore.text = "${item.score} pts"
            lblType.text = if (item.isSensor) "Sensor" else "Buttons"

            return rowView
        }
    }
}