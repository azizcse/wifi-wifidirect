package com.w3.meshlib.common.messages;


import com.w3.meshlib.common.GroupDevice;

public class MessageWrapper {

    public enum MessageType {
        NORMAL, CONNECTION_MESSAGE, DISCONNECTION_MESSAGE, REGISTERED_DEVICES;
    }

    private String message;
    private MessageType messageType;
    private GroupDevice wroupDevice;


    public void setWroupDevice(GroupDevice wroupDevice) {
        this.wroupDevice = wroupDevice;
    }

    public GroupDevice getWroupDevice() {
        return wroupDevice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "MessageWrapper{" +
                "message='" + message + '\'' +
                ", messageType=" + messageType +
                ", wroupDevice=" + wroupDevice +
                '}';
    }

}
