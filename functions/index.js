/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendMessageNotification = functions.firestore
    .document("chats/{chatId}/messages/{messageId}")
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const chatId = context.params.chatId;

        if (!message || message.notificationSent || !message.senderId) return;

        const chatRef = admin.firestore().collection("chats").doc(chatId);
        const chatSnap = await chatRef.get();

        const chat = chatSnap.data();
        const receiverId = chat?.userId === message.senderId ? chat?.dealerId : chat?.userId;

        if (!receiverId) return;

        const userDoc = await admin.firestore().collection("users").doc(receiverId).get();
        const token = userDoc.data()?.fcmToken;

        if (!token) return;

        const payload = {
            data: {
                name: message.name || "New Message",
                text: message.text || "",
                carId: message.carId || "",
                senderId: message.senderId
            }
        };

        try {
            await admin.messaging().sendToDevice(token, payload);
            await snap.ref.update({ notificationSent: true });
        } catch (error) {
            console.error("FCM error:", error);
        }
    });
