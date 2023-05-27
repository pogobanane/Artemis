import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { faCircle, faExpand, faPaperPlane, faXmark } from '@fortawesome/free-solid-svg-icons';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { IrisClientMessage, IrisMessage, IrisMessageContent, IrisMessageContentType, IrisSender } from 'app/entities/iris/iris.model';
import { IrisMessageStore } from 'app/iris/message-store.service';
import { IrisWebsocketService } from 'app/iris/websocket.service';
import { IrisHttpMessageService } from 'app/iris/http-message.service';
import { ActiveConversationMessageLoadedErrorAction, StudentMessageSentAction } from 'app/iris/message-store.model';

@Component({
    selector: 'jhi-exercise-chat-widget',
    templateUrl: './exercise-chat-widget.component.html',
    styleUrls: ['./exercise-chat-widget.component.scss'],
    providers: [IrisHttpMessageService, IrisWebsocketService],
})
export class ExerciseChatWidgetComponent implements OnInit {
    @ViewChild('chatWidget') chatWidget!: ElementRef;
    @ViewChild('chatBody') chatBody!: ElementRef;

    readonly SENDER_USER = IrisSender.USER;
    readonly SENDER_SERVER = IrisSender.SERVER;

    messageStore: IrisMessageStore;
    messages: IrisMessage[] = [];
    newMessage = '';
    isLoading: boolean;
    error = ''; // TODO: error object
    dots = 1;

    constructor(
        private dialog: MatDialog,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private irisHttpMessageService: IrisHttpMessageService,
        private irisWebsocketService: IrisWebsocketService,
    ) {
        this.messageStore = data.messageStore;

        this.irisWebsocketService = irisWebsocketService;
        this.irisWebsocketService.setMessageStore(data.messageStore);
        this.irisWebsocketService.setCallbacks(() => this.scrollToBottom());
    }

    // Icons
    faPaperPlane = faPaperPlane;
    faCircle = faCircle;
    faExpand = faExpand;
    faXmark = faXmark;

    ngOnInit() {
        this.scrollToBottom('auto');
        this.animateDots();
        this.messageStore.getState().subscribe((state) => {
            this.messages = state.messages as IrisMessage[];
            this.isLoading = state.isLoading;
            this.error = state.error;
        });
    }

    animateDots() {
        setInterval(() => {
            this.dots = this.dots < 3 ? (this.dots += 1) : (this.dots = 1);
        }, 500);
    }

    onSend(): void {
        if (this.newMessage) {
            const message = this.newUserMessage(this.newMessage);
            this.messageStore.dispatch(
                new StudentMessageSentAction(message, {
                    error: () => {
                        this.messageStore.dispatch(new ActiveConversationMessageLoadedErrorAction('Something went wrong. Please try again later!'));
                    },
                }),
            );
            this.newMessage = '';
        }
        this.scrollToBottom();
    }

    scrollToBottom(behavior = 'smooth') {
        setTimeout(() => {
            const chatBodyElement: HTMLElement = this.chatBody.nativeElement;
            chatBodyElement.scrollTo({
                top: chatBodyElement.scrollHeight,
                behavior: behavior as ScrollBehavior,
            });
        });
    }

    closeChat() {
        this.dialog.closeAll();
    }

    private newUserMessage(message: string): IrisClientMessage {
        const content: IrisMessageContent = {
            type: IrisMessageContentType.TEXT,
            textContent: message,
        };
        return {
            sender: this.SENDER_USER,
            content: [content],
        };
    }
}
