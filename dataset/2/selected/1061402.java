package com.lb.trac.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import com.lb.trac.pojo.Allegati;
import com.lb.trac.pojo.Evento;
import com.lb.trac.pojo.Profilo;
import com.lb.trac.pojo.StoricoTicket;
import com.lb.trac.pojo.Tickets;
import com.lb.trac.pojo.Utenti;
import com.lb.trac.service.MailSenderService;
import com.lb.trac.service.TracSetupEventMessagePreparator;

public class TicketMailSenderService extends MailSenderService {

    @Override
    public void sendMessage(Object... params) throws Exception {
        sendMessage((Evento) params[0], (Tickets) params[1], (Integer) params[2]);
    }

    /**
	 * Invia il ticket creato agli utenti del progetto
	 * 
	 * @param tickets
	 */
    public void sendMessage(Evento evento, Tickets tickets, int type) throws Exception {
        Utenti utenteSegnalante = tickets.getUtentiByIdUtente();
        StoricoTicket[] st = getSearchService().findMaxStoricoTicketByTicket(new StoricoTicket[0], tickets);
        TracSetupEventMessagePreparator messagePreparator = new TracSetupEventMessagePreparator();
        messagePreparator.setAllegati((Allegati[]) st[0].getAllegatis().toArray(new Allegati[0]));
        messagePreparator.setTickets(tickets);
        messagePreparator.setFrom(getFrom());
        Utenti owner = tickets.getUtentiByIdOwner();
        if (owner == null) {
            Profilo p = getSearchService().findProfiloByDescrizione("DEVELOPER");
            Utenti[] uv = getSearchService().findUtentiAbilitatiProgettoByProfilo(tickets.getIdProgetto().toPlainString(), p);
            for (int i = 0; i < uv.length; i++) {
                messagePreparator.addRecipient(RecipientType.TO, new InternetAddress(uv[0].getEmail()));
            }
        } else {
            messagePreparator.addRecipient(RecipientType.TO, new InternetAddress(owner.getEmail()));
        }
        if (type != SEND_ONLY_TO_DEVELOPER) {
            messagePreparator.addRecipient(RecipientType.TO, new InternetAddress(utenteSegnalante.getEmail()));
        }
        URL url = new URL(getUrl() + "?idTicket=" + tickets.getIdTicket());
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int ch = 0;
        while ((ch = in.read(buffer)) > 0) {
            bout.write(buffer, 0, ch);
        }
        in.close();
        bout.flush();
        bout.close();
        String msg = new String(bout.toByteArray());
        messagePreparator.setMessage(msg);
        getMailSender().send(messagePreparator);
        messagePreparator.setMessageId(messagePreparator.getMimeMessage().getHeader("Message-ID")[0]);
        evento.setEmailMessageId(messagePreparator.getMessageId());
        evento.setDataInvio(new Date());
        evento.setInviato("1");
        if (!isCancellaEventoInviato()) {
            getCustomQueryService().updateEventoInviato(evento);
        } else {
            getCustomQueryService().deleteEventoInviato(evento.getIdEvento());
        }
    }
}
