package com.github.spb.tget.demo.converter;

import com.github.spb.tget.demo.data.Client;
import com.github.spb.tget.demo.data.ContactInformation;
import com.github.spb.tget.demo.dto.AddressDto;
import com.github.spb.tget.demo.dto.ClientDto;
import com.github.spb.tget.demo.dto.ContactInformationDto;
import com.github.spb.tget.demo.dto.PhoneDto;

import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientConverter {

    public Client fromDto(ClientDto clientDto) {
        Client client = new Client();

        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setMiddleName(clientDto.getMiddleName());
        client.setDateOfBirth(
                Date.valueOf(clientDto.getDateOfBirth().toLocalDate().toString()));
        Set<ContactInformation> contactInfoSet = new HashSet<>();

        if (clientDto.getContacts() != null && !clientDto.getContacts().isEmpty()) {
            clientDto.getContacts().forEach(contact -> {
                ContactInformation contactInfo = new ContactInformation();

                contactInfo.setEmail(contact.getEmailAddress());

                if (contact.getPhone() != null) {
                    String phone = String.format("+%d-%s",
                            contact.getPhone().getCountryCode(),
                            contact.getPhone().getPhoneNumber());
                    if (contact.getPhone().getExtension() != null) {
                        phone += String.format(" ext. %d", contact.getPhone().getExtension());
                    }
                    contactInfo.setPhone(phone);
                }

                if (contact.getAddress() != null) {
                    List<String> addressParts = new ArrayList<>();
                    AddressDto addressDto = contact.getAddress();
                    if (!StringUtils.isBlank(addressDto.getCountry())) {
                        addressParts.add(addressDto.getCountry());
                    }
                    if (!StringUtils.isBlank(addressDto.getState())) {
                        addressParts.add(addressDto.getState());
                    }
                    if (!StringUtils.isBlank(addressDto.getPostalCode())) {
                        addressParts.add(addressDto.getPostalCode());
                    }
                    if (!StringUtils.isBlank(addressDto.getAddressLine())) {
                        addressParts.add(addressDto.getAddressLine());
                    }
                    contactInfo.setAddress(String.join("; ", addressParts));
                }

                if (!StringUtils.isAllBlank(contactInfo.getAddress(),
                        contactInfo.getEmail(), contactInfo.getPhone())) {
                    contactInfoSet.add(contactInfo);
                }
            });
        }

        if (!contactInfoSet.isEmpty()) {
            client.setContactInformation(contactInfoSet);
        }

        return client;
    }

    public ClientDto toDto(Client client) {
        ClientDto clientDto = new ClientDto();

        clientDto.setFirstName(client.getFirstName());
        clientDto.setLastName(client.getLastName());
        clientDto.setMiddleName(client.getMiddleName());
        clientDto.setDateOfBirth(client.getDateOfBirth().toLocalDate().atStartOfDay());
        List<ContactInformationDto> contactInformationDtos = new ArrayList<>();

        client.getContactInformation().forEach(ci -> {
            ContactInformationDto contactInformationDto = new ContactInformationDto();

            contactInformationDto.setEmailAddress(ci.getEmail());
            // TODO
            contactInformationDto.setAddress(null);

            PhoneDto phoneDto = new PhoneDto();
            phoneDto.setCountryCode(Integer.parseInt(
                    StringUtils.substringBetween(ci.getPhone(), "+", "-")));
            if (phoneDto.getExtension() != null) {
                phoneDto.setPhoneNumber(StringUtils.substringBetween(ci.getPhone(), "-", " ext."));
                phoneDto.setExtension(Integer.parseInt(StringUtils.substringAfter(ci.getPhone(), " ext. ")));
            } else {
                phoneDto.setPhoneNumber(StringUtils.substringAfter(ci.getPhone(), "-"));
            }
            contactInformationDto.setPhone(phoneDto);
            contactInformationDtos.add(contactInformationDto);
        });

        clientDto.setContacts(contactInformationDtos);
        return clientDto;
    }
}
