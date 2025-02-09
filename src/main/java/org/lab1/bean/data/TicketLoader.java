package org.lab1.bean.data;

import org.lab1.data.entity.Ticket;

import javax.xml.bind.*;
import javax.xml.stream.*;
import org.primefaces.model.file.UploadedFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TicketLoader {

    public List<Ticket> loadTicketsFromFile(UploadedFile uploadedFile) throws IOException {
        List<Ticket> tickets = new ArrayList<>();

        File tempFile = File.createTempFile("ticket", ".xml");
        try (InputStream inputStream = uploadedFile.getInputStream();
             FileOutputStream fos = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }

        int ticketCount = countTicketTags(tempFile);
        System.out.println("Количество тегов <ticket>: " + ticketCount);

        if (ticketCount == 0) {
            throw new IOException("В XML-файле не найдено ни одного тега <ticket>");
        }

        try (InputStream parsingStream = new FileInputStream(tempFile)) {
            JAXBContext context = JAXBContext.newInstance(Ticket.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

            XMLStreamReader reader = factory.createXMLStreamReader(parsingStream);

            while (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamReader.START_ELEMENT && "ticket".equals(reader.getLocalName())) {
                    Ticket ticket = (Ticket) unmarshaller.unmarshal(reader);
                    tickets.add(ticket);
                }
            }

            reader.close();
        } catch (XMLStreamException | JAXBException e) {
            throw new IOException("Ошибка при разборе XML файла: " + e.getMessage(), e);
        }

        return tickets;
    }

    private int countTicketTags(File xmlFile) throws IOException {
        int count = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        try (InputStream inputStream = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamReader.START_ELEMENT && "ticket".equals(reader.getLocalName())) {
                    count++;
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new IOException("Ошибка при чтении XML файла для подсчета тегов <ticket>: " + e.getMessage(), e);
        }

        return count;
    }
}
