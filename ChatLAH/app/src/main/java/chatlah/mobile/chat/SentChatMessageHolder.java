package chatlah.mobile.chat;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import chatlah.mobile.R;
import chatlah.mobile.chat.model.ChatMessage;

public class SentChatMessageHolder extends ChatMessageHolder{

    private TextView chatMessageBody;
    private TextView chatMessageTime;

    public SentChatMessageHolder(@NonNull View itemView) {
        super(itemView);
        chatMessageBody = itemView.findViewById(R.id.chat_message_body);
        chatMessageTime = itemView.findViewById(R.id.chat_message_time);
    }

    public void bind(ChatMessage chatMessage) {
        chatMessageBody.setText(chatMessage.getMessage());
        chatMessageTime.setText(chatMessage.getTimestamp().toDate().toString());
    }
}
