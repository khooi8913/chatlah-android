package chatlah.mobile;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chatlah.mobile.chat.ChatMessageHolder;
import chatlah.mobile.chat.model.ChatMessage;

public class ReceivedChatMessageHolder extends ChatMessageHolder {

    private ImageView senderImage;
    private TextView senderName;
    private TextView chatMessageBody;
    private TextView chatMessageTime;

    public ReceivedChatMessageHolder(@NonNull View itemView) {
        super(itemView);
        senderImage = itemView.findViewById(R.id.chat_message_sender_image);
        senderName = itemView.findViewById(R.id.chat_message_sender_name);
        chatMessageBody = itemView.findViewById(R.id.chat_message_body);
        chatMessageTime = itemView.findViewById(R.id.chat_message_time);
    }

    public void bind(ChatMessage chatMessage) {
        // TODO: senderImage
        senderName.setText(chatMessage.getSender());
        chatMessageBody.setText(chatMessage.getMessage());
        chatMessageTime.setText(chatMessage.getTimestamp().toDate().toString());
    }
}
