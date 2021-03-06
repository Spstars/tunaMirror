package com.example.aacrawler

import android.app.AlertDialog
import android.content.ClipData
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aacrawler.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup


class MainActivity : AppCompatActivity() {
    lateinit var adapter:MyAdapter
    lateinit var binding:ActivityMainBinding
    val scope= CoroutineScope(Dispatchers.IO)
    val Tunalists=ArrayList<TunaData>()
    var tunaName=""
    var onlyMaster=false
    var searchTuna=false
    var previousUrl=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        if(savedInstanceState!=null){
            Tunalists.addAll(savedInstanceState.getParcelableArrayList<TunaData>("Tuna")!!)
            adapter.items.addAll(Tunalists)
            adapter.notifyDataSetChanged()
        }

    }

    fun init(){

        binding.aaRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        //       binding.aaRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.aaRecyclerView.addItemDecoration(VerticalItemDecorator(20))
        binding.aaRecyclerView.addItemDecoration(HorizontalItemDecorator(10))

        adapter= MyAdapter(ArrayList(),12F)
        //      adapter.items.add(TunaData("",""))
        binding.aaRecyclerView.adapter=adapter

        binding.button.setOnClickListener {
            var pageurl=""
            var url=binding.urlEdit.text.toString()
            if(url==""){
                if(previousUrl==""){
                    Toast.makeText(this,"url??? ??????????????????",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }else{
                    Toast.makeText(this,"?????? ????????? ????????? ???????????????",Toast.LENGTH_SHORT).show()
                    url=previousUrl
                }
            }
            if (!url.startsWith("http://") && url.isDigitsOnly()){
                pageurl="https://bbs.tunaground.net/trace.php/anchor/"+url
            }
            else if (pageurl.startsWith(">")){
                var tmp=pageurl.replace(">","/")
                tmp.replace("-","/")
                pageurl="https://bbs.tunaground.net/trace.php/anchor/"+url
            }
            else{ //?????? ????????? ????????? ?????? ?????? ????????? ????????? ?????? ?????? ????????? ?????? ????????? ?????? ???????????? ?????? ??????
                pageurl=url
                previousUrl=pageurl
                Log.i("?????? ??????",previousUrl)
            }
            // pageurl="https://bbs.tunaground.net/trace.php/anchor/1596242937//"

            crawlTuna(pageurl)
        }
    }

    fun crawlTuna(pageurl: String) {
        adapter.items.clear()
        Tunalists.clear()
        adapter.notifyDataSetChanged()
        Log.i("Url",pageurl)
        scope.launch {

            val doc= Jsoup.connect(pageurl).get()
                .outputSettings(org.jsoup.nodes.Document.OutputSettings().prettyPrint(false))

            val masterCode=doc.select("p.thread_owner").text().replace("Manage","").split("???")
            Log.i("master",masterCode[0]+masterCode[1])

            val headlines=doc.select("div.thread_body>div")

            //          val newspaper=doc.select("div.thread_body")
            //         var tmp3 :String=news.select("div.response").select("p.response_info").select("span.response_owner").toString()
            val regax=Regex("""<span style=".*?">""")
            val regax2=Regex("""</span>""")
            val regax3=Regex("""\s<p class="mona">""")
            //???????????? ??????????????? ??????  ^div class="content">  ^\s.*</div>
            // var regex3 = /(?<=????????? )[0-9]+/g; ???????????? ->????????? xxxx???

            val regax4=Regex("""(?<=<div class="content">\n)\s+""")
            val regax5=Regex("""\s<p class="mona">""")


            for (news in headlines){
                val count=news.select("span.response_sequence").text()
                val tmp1=news.select("p.response_info>span.response_owner").text()
                //               Log.i("AA",news.select("div.content").toString().replace("<br>","\n"))
                val tmp2=news.select("div.content").toString()
                    .replace("<br>","\n")
                    .replace(regax4,"")
                    .replace("<div class=\"content\">","")
                    .replace(regax3,"")
                    .replace(regax5,"")
                    .replace("</div>","")
                    .replace("</p>","")
                    .replace("&gt;",">>")
                    .replace(regax,"")
                    .replace(regax2,"")
                    .replace("&lt","<")

//                Log.i("?????????",tmp1)
//                Log.i("?????????2",tmp2)
                if(onlyMaster) { //?????? ???????????? tmp1??? mastercode??? ?????????????????? ??????????????????
                    if (tmp1.contains(masterCode[0])) {
                        Tunalists.add(TunaData(count.toString() + " " + tmp1, tmp2))
                    }
                }
                else if (searchTuna){
                    Log.i("searchTuna",tunaName+" "+tmp1)
                    val tmp3=tmp1.split("???")
                    if(tunaName==tmp3[0]){
                        Tunalists.add(TunaData(count.toString() + " " + tmp1, tmp2))
                    }
                }
                else{
                    Tunalists.add(TunaData(count.toString() + " " + tmp1, tmp2))
                }
            }

            withContext(Dispatchers.Main){
                adapter.items.addAll(Tunalists)
                adapter.notifyDataSetChanged()
                Log.i("adapter Size",adapter.items.size.toString())
            }

        }
        binding.urlEdit.setText("")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("Tuna",Tunalists)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.smallText->{
                adapter= MyAdapter(Tunalists,10F)
                binding.aaRecyclerView.adapter=adapter
            }
            R.id.defaultText->{
                adapter= MyAdapter(Tunalists,12F)
                binding.aaRecyclerView.adapter=adapter
            }
            R.id.bigText->{
                adapter= MyAdapter(Tunalists,14F)
                binding.aaRecyclerView.adapter=adapter
            }
            R.id.onlyMaster->{
                onlyMaster=!onlyMaster
                item.setChecked(onlyMaster)
                Log.i("?????? ????????????",onlyMaster.toString())

            }
            R.id.searchOption->{
                val builder = AlertDialog.Builder(this)

                var v1 = layoutInflater.inflate(R.layout.optiondialog, null)
                builder.setView(v1)

                var edit1: EditText? = v1.findViewById<EditText>(R.id.inputNamae)
                var Text: TextView? = v1.findViewById<TextView>(R.id.textView2)
                // p0??? ?????? AlertDialog??? ????????????. findViewById??? ?????? view??? ???????????? ??????


                builder.setPositiveButton("??????"){ dialogInterface, i ->
                    if (edit1 != null) {
                        onlyMaster=false
                        tunaName=edit1.text.toString()
                        searchTuna=true
                    }
                }
                builder.setNegativeButton("??????", null)
//
//                if (onlyMaster){
//                    builder.setNeutralButton("?????? ?????? ??????"){dialogInterface, i->
//                        if (edit1!=null){
//                            onlyMaster=false
//                            searchTuna=false
//                        }
//                    }
//                }
//                else{
//                    builder.setNeutralButton("Just ?????????"){dialogInterface, i->
//                        if (edit1!=null){
//                            onlyMaster=true
//                            searchTuna=false
//                        }
//                    }
//                }
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

// Log.i("tmp1",newspaper.select("div.content").toString().replace("<br>","\n"))
//            val tmp1=newspaper.select("div.content").toString()
//                    .replace("<br>","\n")
//                    .replace(regax4,"")
//                    .replace("<div class=\"content\">","")
//                    .replace(regax3,"")
//                    .replace(regax5,"")
//                    .replace("</div>","")
//                    .replace("</p>","")
//                    .replace("&gt;",">>")
//                    .replace(regax,"")
//                    .replace(regax2,"")