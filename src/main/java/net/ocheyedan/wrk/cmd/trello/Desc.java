package net.ocheyedan.wrk.cmd.trello;

import net.ocheyedan.wrk.Output;
import net.ocheyedan.wrk.RestTemplate;
import net.ocheyedan.wrk.cmd.Args;
import net.ocheyedan.wrk.cmd.Usage;
import net.ocheyedan.wrk.trello.*;
import org.codehaus.jackson.type.TypeReference;

import java.util.Collections;
import java.util.Map;

/**
 * User: blangel
 * Date: 7/1/12
 * Time: 7:57 AM
 */
public final class Desc extends IdCommand {

    private static enum Type {
        Org, Board, List, Card, Member
    }

    private final String url;

    private final String description;

    private final Type type;

    public Desc(Args args) {
        super(args);
        if (args.args.size() == 1) {
            TrelloId id = parseWrkId(args.args.get(0), allPrefix);
            if (id.idWithTypePrefix.startsWith("o:")) {
                String orgId = id.idWithTypePrefix.substring(2);
                url = Trello.url("https://trello.com/1/organizations/%s?key=%s&token=%s", orgId,
                        Trello.APP_DEV_KEY, Trello.USR_TOKEN);
                description = String.format("Description of organization ^b^%s^r^:", orgId);
                type = Type.Org;
            } else if (id.idWithTypePrefix.startsWith("b:")) {
                String boardId = id.idWithTypePrefix.substring(2);
                url = Trello.url("https://trello.com/1/boards/%s?key=%s&token=%s", boardId,
                        Trello.APP_DEV_KEY, Trello.USR_TOKEN);
                description = String.format("Description of board ^b^%s^r^:", boardId);
                type = Type.Board;
            } else if (id.idWithTypePrefix.startsWith("l:")) {
                String listId = id.idWithTypePrefix.substring(2);
                url = Trello.url("https://trello.com/1/lists/%s?key=%s&token=%s", listId,
                        Trello.APP_DEV_KEY, Trello.USR_TOKEN);
                description = String.format("Description of list ^b^%s^r^:", listId);
                type = Type.List;
            } else if (id.idWithTypePrefix.startsWith("c:")) {
                String cardId = id.idWithTypePrefix.substring(2);
                url = Trello.url("https://trello.com/1/cards/%s?key=%s&token=%s", cardId,
                        Trello.APP_DEV_KEY, Trello.USR_TOKEN);
                description = String.format("Description of card ^b^%s^r^:", cardId);
                type = Type.Card;
            } else if (id.idWithTypePrefix.startsWith("m:")) {
                String memberId = id.idWithTypePrefix.substring(2);
                url = Trello.url("https://trello.com/1/members/%s?key=%s&token=%s", memberId,
                        Trello.APP_DEV_KEY, Trello.USR_TOKEN);
                description = String.format("Description of member ^b^%s^r^:", memberId);
                type = Type.Member;
            } else {
                url = description = null;
                type = null;
            }
        } else {
            url = description = null;
            type = null;
        }
    }

    @Override protected Map<String, String> _run() {
        Output.print(description);
        String desc;
        switch (type) {
            case Org:
                Organization org = RestTemplate.get(url, new TypeReference<Organization>() { });
                if (org == null) {
                    Output.print("^red^Invalid id or not found.^r^");
                    break;
                }
                Output.print("  ^b^%s^r^ ^black^| %s^r^", org.getDisplayName(), org.getId());
                desc = (org.getDesc() == null ? "" : org.getDesc());
                if (!desc.isEmpty()) {
                    Output.print("    %s", desc);
                }
                Output.print("    ^black^%s^r^", org.getUrl());
                break;
            case Board:
                Board board = RestTemplate.get(url, new TypeReference<Board>() { });
                if (board == null) {
                    Output.print("^red^Invalid id or not found.^r^");
                    break;
                }
                String boardClosed = ((board.getClosed() != null) && board.getClosed()) ? "^black^[closed] ^r^" : "^b^";
                Output.print("  %s%s^r^ ^black^| %s^r^", boardClosed, board.getName(), board.getId());
                desc = (board.getDesc() == null ? "" : board.getDesc());
                if (!desc.isEmpty()) {
                    Output.print("    %s", desc);
                }
                Output.print("    ^black^%s^r^", board.getUrl());
                break;
            case List:
                net.ocheyedan.wrk.trello.List list = RestTemplate.get(url, new TypeReference<net.ocheyedan.wrk.trello.List>() { });
                if (list == null) {
                    Output.print("^red^Invalid id or not found.^r^");
                    break;
                }
                String closed = ((list.getClosed() != null) && list.getClosed()) ? "^black^[closed] ^r^" : "^b^";
                Output.print("  %s%s^r^ ^black^| %s^r^", closed, list.getName(), list.getId());
                break;
            case Card:
                Card card = RestTemplate.get(url, new TypeReference<Card>() { });
                if (card == null) {
                    Output.print("^red^Invalid id or not found.^r^");
                    break;
                }
                String labels = Cards.buildLabel(card.getLabels());
                String cardClosed = ((card.getClosed() != null) && card.getClosed()) ? "^black^[closed] ^r^" : "^b^";
                Output.print("  %s%s^r^%s ^black^| %s^r^", cardClosed, card.getName(), labels, card.getId());
                desc = (card.getDesc() == null ? "" : card.getDesc());
                if (!desc.isEmpty()) {
                    Output.print("    %s", desc);
                }
                Output.print("    ^black^%s^r^", Cards.getPrettyUrl(card));
                break;
            case Member:
                Member member = RestTemplate.get(url, new TypeReference<Member>() { });
                if (member == null) {
                    Output.print("^red^Invalid id or not found.^r^");
                    break;
                }
                Output.print("  ^b^%s^r^ ^black^| %s^r^", member.getFullName(), member.getId());
                Output.print("    ^black^username^r^ %s", member.getUsername());
                break;
        }
        return Collections.emptyMap();
    }

    @Override protected boolean valid() {
        return (url != null);
    }

    @Override protected String getCommandName() {
        return "desc";
    }
}
