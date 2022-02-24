package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var ChatrecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<message>
    private lateinit var mDbRef: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val sendUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + sendUid
        receiverRoom = sendUid + receiverUid

        supportActionBar?.title = name

        ChatrecyclerView = findViewById(R.id.ChatrecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendByImage)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        ChatrecyclerView.layoutManager = LinearLayoutManager(this)
        ChatrecyclerView.adapter = messageAdapter

        //logic for adding data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(postSnapshot in snapshot.children) {
                        val Message = postSnapshot.getValue(message::class.java)
                        messageList.add(Message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


                //adding the message to database
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = message(message, sendUid)
            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")
        }
    }
}