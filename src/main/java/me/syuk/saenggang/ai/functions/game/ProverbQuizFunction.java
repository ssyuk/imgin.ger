package me.syuk.saenggang.ai.functions.game;

import com.google.gson.JsonObject;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProverbQuizFunction implements AIFunction {
    public static final List<Proverb> PROVERB_LIST = Arrays.asList(
            new Proverb("가는 날이/'장날'이다", "뜻하지 않은 일이 우연하게도 잘 들어 맞았을 때 쓰는 말"),
            new Proverb("가는 말이 고와야/'오는 말'이 '곱'다", "내가 남에게 좋게 해야 남도 내게 잘 한다는 말"),
            new Proverb("가랑비에/'옷 젖는 줄 모른'다", "재산 같은 것이 조금씩 조금씩 없어지는 줄 모르게 줄어 들어가는 것을 뜻함"),
            new Proverb("가랑잎이 솔잎더러/'바스락'거린다고 한다", "제 결점이 큰 줄 모르고 남의 작은 허물을 탓한다는 말,"),
            new Proverb("가재는 게/'편'이다", "됨됨이나 형편이 비슷하고 인연 있는 것끼리 서로 편이 되어 어울리고 사정을 보아 춤을 이르는 말"),
            new Proverb("가지 많은 나무에/'바람' 잘 날 '없다'", "자식 많은 사람은 걱정이 떠날 때가 없다는 뜻"),
            new Proverb("간에 가 붙고/'쓸개'에 '가' '붙'는다", "제게 조금이라도 이로운 일이라면 체면과 뜻을 어기고, 아무에게나 아첨한다는 뜻"),
            new Proverb("간에/'기별'도 '안 간'다", "음식을 조금밖에 먹지 못하여 제 양에 차지 않을 때 쓰는 말"),
            new Proverb("간이/'콩알'만 '해'지다", "겁이 나서 몹시 두려워진다는 뜻"),
            new Proverb("갈수록/'태산'", "어려운 일을 당하면 당할수록 점점 어려운 일이 닥쳐 온다는 뜻"),
            new Proverb("값싼 것이/'비지떡'", "무슨 물건이고 값이 싸면 품질이 좋지 못하다는 뜻"),
            new Proverb("같은 값이면/'다홍치마'", "이왕 같은 값이면 자기에게 소득이 많은 것으로 택한다는 말"),
            new Proverb("개구리 올챙이/'적' '생각'을 '못' 한다", "자기의 지위가 높아지면 전날의 미천하던 때의 생각을 못 한다는 뜻"),
            new Proverb("개밥에/'도토리'", "여럿 속에 어울리지 못하는 사람을 뜻하는 말"),
            new Proverb("개천에서/'용' 난다", "변변하지 못한 집안에서 훌륭한 인물이 나왔을 때 쓰는 말"),
            new Proverb("고기는 씹어야 맛이요/'말'은 '해'야 '맛'이라", "마음속으로만 끙끙거리고 애타하지 말고 할 말은 속 시원히 해야 한다는 말,"),
            new Proverb("고래 싸움에/'새우 등' '터진'다", "힘센 사람들끼리 서로 싸우는 통에 공연히 약한 사람이 그 사이에 끼여 아무 관계없이 해를 입을 때 쓰는 말"),
            new Proverb("고양이 목에/'방울' 달기", "실행하기 어려운 일을 공연히 의논하을 빗대어 이르는 말"),
            new Proverb("공든 탑이/'무너'지라", "힘을 다하고 정성을 다하여 한 일은 헛되지 않아 반드시 좋은 결과를 얻는다는 뜻"),
            new Proverb("구더기 무서워/'장' '못' '담'글까", "다소 방해되는 일이 있다 하더라도 마땅히 할 일은 해야 한다는 말"),
            new Proverb("구슬이 서 말이라도/'꿰어'야 '보배'라", "아무리 훌륭한 일이라도 완전히 끝을 맺어 놓아야 비로소 가치가 있다는 말"),
            new Proverb("귀에 걸면 귀걸이/'코'에 '걸면' '코걸이'", "한 가지의 것이 이런 것도 같고 저런 것도 같아  어느 한 쪽으로 결정짓기 어려운 일을 두고 하는 말,"),
            new Proverb("그림의/'떡'", "보기는 하여도 먹을 수도 업고 가질 수도 없어 실제에 아무 소용이 없는 경우를 이르는 말"),
            new Proverb("금강산도/'식후경'", "아무리 좋은 것, 재미있는 일이 있더라도 배가 부르고 난 뒤에야 좋은 줄 안다  곧, 먹지 않고는 좋은 줄 모른다는 뜻"),
            new Proverb("뛰는 놈 위에/'나는 놈' 있다", "아무리 재주가 있다 하여도 그보다 나은 사람이 있는 것이니 너무 자랑하지 말라는 뜻"),
            new Proverb("까마귀 날자/'배 떨어'진다", "아무 관계없이 한 일이 공교롭게도 다른 일과 때를 같이 하여 둘 사이에 무슨 관계라도 있는 듯한 의심을 받을 때 쓰는 말"),
            new Proverb("꿩 대신/'닭'", "자기가 쓰려는 것이 없을 때, 그와 비슷한 것으로 대신 쓸 수도 있다는 말"),
            new Proverb("꿩 먹고/'알 먹'기", "한 가지 일을 하고 두 가지 이익을 볼 때 쓰는 말 남의 잔치에 감 놓아라 배 놓아라 한다"), new Proverb("", "쓸데없이 남의 일에 간섭한다는 뜻"),
            new Proverb("낫 놓고/'기역 자'도 '모른'다", "글자라고는 아무것도 모르는 몹시 무식한 사람을 두고 하는  말"),
            new Proverb("낮말은 새가 듣고/'밤말'은 '쥐'가 '듣는'다", "아무리 비밀히 하는 말도 새어 나가기 쉬우니,  알을 항상 조심해서 하라는 뜻"),
            new Proverb("내 코가/'석자'", "내 사정이 급해서 남의 사정까지 돌볼 수가 없다는 말"),
            new Proverb("누워서/'침 뱉기'", "남을 해치려다 도리어 자기 자신이 해를 입는다는 말"),
            new Proverb("늦게 배운 도둑이/'날 새는 줄' '모른'다", "나이 들어서 시작한 일에 몹시 골몰한 사람을 두고 이름"),
            new Proverb("다 된 축에/'코 풀기'", "다 된 일을 망쳐 놓았다는 뜻"),
            new Proverb("달면 삼키고/'쓰면' '뱉는'다", "제게 이로우면 이용하며, 필요하지 않을 때에는 버린다는 뜻"),
            new Proverb("닭 잡아먹고/'오리발' '내민'다", "나쁜 일을 하고 간사한 꾀로 숨기려 할 때 쓰는 말"),
            new Proverb("도둑이/'제발 저리'다", "죄지은 자가 그것이 폭로될까 두려워하는 나머지 알지 못하는 가운데 그것을 나타내고야 만다는 뜻"),
            new Proverb("돌다리도/'두들겨' 보고 건너라", "아무리 잘 아는 일이라도 조심하여 실수 없게 하라는 뜻"),
            new Proverb("되로 주고/'말로 받'는다", "남을 조금 건드렸다가 도리어 일을 크게 당한다는 뜻"),
            new Proverb("등잔 밑이/'어둡다'", "제게 가까운 일을 먼 데 일보다 오히려 모른다는 뜻"),
            new Proverb("땅 짚고/'헤엄치기'", "땅을 짚고 헤엄치듯이 아주 쉽게 할 수 있는 일을 가리켜 하는 말"),
            new Proverb("똥 묻은 개가/'겨' '묻은' '개' '나무'란다", "자기는 더 큰 흉이 있으면서 도리어 남의 작은 흉을 탓한다는 뜻"),
            new Proverb("마른하늘에/'날벼락'", "뜻밖에 입는 재난을 이르는 말"),
            new Proverb("말 한마디에/'천 냥 빚'도 '갚'는다", "말을 잘 하면 큰 빚도 갚을 수 있다는 말로, 말의 중요성을 나타낸 말"),
            new Proverb("목구멍이/'포도청'", "먹고살기 위해서는 어떤 일이라도 하게 된다는 뜻"),
            new Proverb("못된 송아지/'엉덩이'에 '뿔' 난다", "되지 못한 사람이 건방지고 좋지 못한 짓을 한다는 뜻"),
            new Proverb("믿는 도끼에/'발등' '찍'힌다", "믿던 일이 뜻밖에 실패한다는 말"),
            new Proverb("밑 빠진 독에/'물 붓'기", "아무리 노력을 하고 애써도 보람이 나타나지 않는 경우에 쓰는 말"),
            new Proverb("바늘 도둑이/'소 도둑' 된다", "나쁜 행실일수록 점점 더 크고 심하게 되니 아예 나쁜 버릇은 길들이지 말라는 뜻"),
            new Proverb("배보다/'배꼽'이 더 '크'다", "마땅히 작아야 할 것이 오히려 클 때를 비유해서 이르는 말"),
            new Proverb("백지장도/'맞들'면 '낫'다", "아무리 쉬운 일이라도 혼자 하는 것보다 협력하여 하는 것이 훨씬 더 낫다는 말"),
            new Proverb("벼룩의/'간' '빼먹'기", "극히 적은 이익을 부당한 수단을 써서 착취한다는 말"),
            new Proverb("병 주고/'약' 준다", "일이 안 되도록 방해하고는 도와주는 척한다는 뜻"),
            new Proverb("보기 좋은 떡이/'먹기'도 '좋'다", "겉모양이 좋으면 속의 내용도 좋다는 뜻"),
            new Proverb("빛 좋은/'개살구'", "겉만 번지르하고 실속이 없다는 뜻"),
            new Proverb("사공이 많으면/'배'가 '산'으로 올라'간다'", "간섭하는 사람이 많으면 일이 잘 안된다는 뜻"),
            new Proverb("새 발의/피", "지극히 적은 분량을 말함"),
            new Proverb("서당 개 삼 년에/'풍월'을 '읊는'다", "무식한 사람이라도 유식한 사람과 같이 오래 지내면 자연히 견문이 생긴다는 말"),
            new Proverb("세 살 버릇/'여든'까지 '간'다", "어려서부터 좋은 버릇을 들여야 한다는 뜻"),
            new Proverb("소문난 잔치에/'먹을 것' '없'다", "소문난 것이 흔히 실지로는 보잘것없다는 말"),
            new Proverb("소 잃고/'외양간' '고'친다", "이미 일을 그르치고 난 뒤 뉘어쳐도 소용이 없다는 뜻"),
            new Proverb("소뿔도/'단김'에 '빼라' 했다", "어떤 일을 하려고 생각하였으면 망설이지 알고 곧 행동으로 옮기라는 뜻"),
            new Proverb("수박/'겉핥기'", "내용이나 참뜻은 모르면서 대충 일하는 것을 비유해서 쓰는 말"),
            new Proverb("식은/'죽 먹기'", "어떤 일이 아주 하기 쉽다는 말"),
            new Proverb("십 년이면/'강산'도 '변한'다", "십 년이란 세월이 흐르면 세상에 변하지 않는 것이 없다는 말"),
            new Proverb("아는 길도/'물어가라'", "아무리 익숙한 일이라도 남에게 물어보고 조심함이 안전하다는 뜻"),
            new Proverb("아니 땐/'굴뚝'에 '연기' 나라", "반드시 원인이 있어야 결과가 생긴다는 뜻"),
            new Proverb("아닌 밤중에/'홍두깨'", "예고도 없이 뜻밖의 일이 생겼을 때 하는 말"),
            new Proverb("약방에/'감초'", "어떤 일에나 빠진 없이 참여하는 사람을 말함"),
            new Proverb("어물전 망신은/'꼴뚜기'가 '시킨'다", "못난 자일수록 그와 같이 있는 동료를 망신시킨다는 말"),
            new Proverb("열 길 물속은 알아도/'한' 길 '사람 속'은 '모른'다", "사람의 마음은 알아내기가 어렵다는 뜻"),
            new Proverb("열 번 찍어/아니 '넘어가는' '나무' '없다'", "여러 번 계속해서 애쓰면 어떤 일이라도 이룰 수 있다는 뜻"),
            new Proverb("오뉴월 감기는/'개'도 '아니' '앓'는다", "여름철에 감기 걸린 사람을 조롱하는 말"),
            new Proverb("오르지 못할 나무는/'쳐다'보지도 '말'아라", "될 수 없는 일은 바라지도 말라는 뜻"),
            new Proverb("옥에/'티'", "아무리 좋아도 한 가지 결점은 있다는 말"),
            new Proverb("우물에 가서/'숭늉' '찾'는다", "일의 순서도 모르고 성급하게 덤빈다는 뜻"),
            new Proverb("울며/'겨자' 먹기", "싫은 일을 좋은 척하고 억지로 하지 않을 수 없는 경우를 나타내는 말"),
            new Proverb("원수는/'외나무다리'에서 만난다", "남에게 악한 일을 하면 그 죄를 받을 때가 반드시 온 다는 뜻"),
            new Proverb("원숭이도/'나무'에서 '떨어'진다", "아무리 능숙한 사람도 실수할 때가 있다는 말"),
            new Proverb("윗물이 맑아야/'아랫물'도 '맑'다", "윗사람이 잘못하면 아랫사람도 따라서 잘못하게 된다는 뜻"),
            new Proverb("자라 보고 놀란 가슴/'솥뚜껑' '보고' '놀란'다", "무엇에 한 번 혼난 사람이 그와 비슷한 것만 보아도 깜짝 놀란다는 말"),
            new Proverb("자랄 나무는/'떡잎'부터 '알아본'다", "앞으로 크게 될 사람은 어려서부터 장래성이 엿보인 다는 말,"),
            new Proverb("작은 고추가/더 '맵다'", "겉으로는 대수롭지 않게 보이는 사람이 하는 일이 더 다부지다는 뜻"),
            new Proverb("종로에서 뺨 맞고/'한강' 가서 '눈 흘긴'다", "욕을 당한 자리에서는 아무 말도 못 하고  딴 데 가서 화풀이를 한다는 뜻"),
            new Proverb("좋은 약은/'입에 쓰'다", "듣기 싫고 귀에 거슬리는 말이라도 제 인격 수양에는 이롭다는 뜻"),
            new Proverb("쥐구멍에도/'볕 들 날'이 '있'다", "아무리 고생만 하는 사람도 운수가 터져 좋은 시기를 만날 때가 있다는 말"),
            new Proverb("지렁이도/'밟으'면 '꿈틀'한다", "아무리 보잘것없는 사람이라도 너무나 업신여기면 성을 낸다는 뜻"),
            new Proverb("천 리 길도/'한 걸음'부터", "무슨 일이든 그 시초가 중요하다는 뜻"),
            new Proverb("칼로/'물 베기'", "다투다가도 좀 시간이 흐르면 이내 풀려 두 사람 사이에 아무 틈이 생기지 않는다는 뜻"),
            new Proverb("콩 심은 데 콩 나고/'팥 심은 데 팥' 난다", "모든 일은 원인에 따라 결과가 생긴다는 말"),
            new Proverb("티끌 모아/'태산'", "작은 거이라도 모이면 큰 것이 된다는 뜻"),
            new Proverb("핑계 없는/'무덤 없'다", "무엇을 잘 못해 놓고도 여러 가지 이유로 책임을 회피하려는  사람을 두고 하는 말"),
            new Proverb("하늘의/'별 따기'", "지극히 어려운 일을 두고 하는 말"),
            new Proverb("하늘이 무너져도/'솟아날 구멍'이 있다", "아무리 큰 재난에 부딪히더라도 그것에서 벗어날 길은 있다는 뜻"),
            new Proverb("하룻강아지/'범 무서운 줄' 모른다", "아직 철이 없어서 아무것도 모르는 것을 두고 하는  말"),
            new Proverb("한 귀로 듣고/'한 귀로 흘린'다", "남이 애써 일러 주는 말을 유념해서 듣지 않고 건성으로 듣는 것을 이름"),
            new Proverb("한 술 밥에/'배부르'라", "무슨 일이고 처음에는 큰 성과를 기대할 수 없다는 말, 힘을 조금 들이고는 큰 효과를 바랄 수 없다는 이야기"),
            new Proverb("호랑이도/'제 말' 하면 '온'다", "마침 이야기하고 있는데 그 장본인이 나타났을 때 하는 말로,  자리에 사람이 없다고 하여 남의 흉을 함부로 보지 말라는 뜻")
    );

    public static List<String> extractWordsInQuotes(String input) {
        List<String> wordsInQuotes = new ArrayList<>();

        Pattern pattern = Pattern.compile("'(.*?)'");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            wordsInQuotes.add(matcher.group(1));
        }

        return wordsInQuotes;
    }

    @Override
    public String name() {
        return "proverb_quiz";
    }

    @Override
    public String description() {
        return "일부분이 지워진 속담이 주어지면, 지워진 부분에 들어갈 말을 맞추는 게임. 속담퀴즈";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        requestMessage.reply("네! 속담퀴즈를 시작할게요! 위 Thread로 들어와주세요!");
        ServerThreadChannel channel = Utils.createGameThread(requestMessage, "속담퀴즈");

        channel.sendMessage("""
                일부분이 지워진 속담이 주어집니다. 지워진 부분에 들어갈 말을 입력해주세요! (필수 단어만 포함되면 정답 인정됩니다.)
                그만하고 싶으시면 `그만`이라고 말해주세요!
                스킵하고 싶으시면 `스킵`이라고 말해주세요! (스킵하더라도 연속 정답이 초기화되지 않습니다.)
                **3번 연속 정답을 맞추면 25코인을 드립니다!**
                """);
        AtomicReference<Proverb> proverb = new AtomicReference<>(PROVERB_LIST.get((int) (Math.random() * PROVERB_LIST.size())));
        AtomicReference<String> prov = new AtomicReference<>(proverb.get().proverb().split("/")[0]);
        AtomicReference<String> answer = new AtomicReference<>(proverb.get().proverb().split("/")[1]);
        channel.sendMessage("속담퀴즈 내드릴게요!\n" +
                "# " + prov + " " + "___\n" +
                "뜻: **" + proverb.get().description() + "**");

        AtomicInteger count = new AtomicInteger();
        MessageCreated.replyCallbackMap.put(account, replyMessage -> {
            if (replyMessage.getChannel().getId() != channel.getId()) return false;

            return CompletableFuture.supplyAsync(() -> {
                String content = replyMessage.getContent().replace(" ", "");
                boolean isAnswer = true;
                for (String extractWordsInQuote : extractWordsInQuotes(answer.get())) {
                    if (!content.contains(extractWordsInQuote.replace(" ", ""))) {
                        isAnswer = false;
                        break;
                    }
                }

                if (replyMessage.getContent().equals("그만")) {
                    channel.sendMessage("속담퀴즈를 종료합니다.");
                    MessageCreated.replyCallbackMap.remove(account);
                    channel.createUpdater().setArchivedFlag(true).update();
                    return true;
                } else if (replyMessage.getContent().equals("스킵")) {
                    channel.sendMessage("**정답: " + answer.get().replace('/', ' ').replace("'", "") + "**");
                } else if (isAnswer) {
                    channel.sendMessage("정답입니다! 축하드려요!\n" +
                            "**" + proverb.get().proverb().replace('/', ' ').replace("'", "") + "**: " + proverb.get().description());
                    count.incrementAndGet();
                    if (count.get() % 3 == 0) {
                        account.giveCoin(replyMessage.getChannel(), 25, "연속 정답 횟수가 " + count.get() + "회가 되서");
                    }
                } else {
                    count.set(0);
                    channel.sendMessage("틀렸어요! 연속 정답 횟수가 초기화되었습니다. 다시 시도해보세요!\n" + "그만하고 싶으시면 `그만`이라고, 스킵하고 싶으시면 `스킵`이라고 말해주세요!");
                    return true;
                }

                int at = (int) (Math.random() * PROVERB_LIST.size());
                proverb.set(PROVERB_LIST.get(at));
                prov.set(proverb.get().proverb().split("/")[0]);
                answer.set(proverb.get().proverb().split("/")[1]);
                channel.sendMessage("다음 문제를 내드릴게요! (연속 정답: " + count.get() + "회)\n" +
                        "# " + prov + " " + "___\n" +
                        "뜻: **" + proverb.get().description() + "**\n" +
                        "그만하고 싶으시면 `그만`이라고, 스킵하고 싶으시면 `스킵`이라고 말해주세요!");
                return true;
            }).join();
        });

        return null;
    }

    public record Proverb(String proverb, String description) {
    }
}
