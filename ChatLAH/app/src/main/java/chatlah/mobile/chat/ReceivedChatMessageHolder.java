package chatlah.mobile.chat;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import chatlah.mobile.Identicon;
import chatlah.mobile.R;
import chatlah.mobile.chat.model.ChatMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReceivedChatMessageHolder extends ChatMessageHolder {

    private CircleImageView senderImage;
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

        senderImage.setImageBitmap(Identicon.create(chatMessage.getSender()));

        // No longer required
        senderName.setText(chatMessage.getSender());

        chatMessageBody.setText(chatMessage.getMessage());
        chatMessageTime.setText(
                new SimpleDateFormat("HH:mm ").format(chatMessage.getTimestamp().toDate()
        ));
    }
}
