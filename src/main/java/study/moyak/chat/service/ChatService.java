package study.moyak.chat.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.moyak.chat.dto.ChatDTO;
import study.moyak.chat.dto.EachPillDTO;
import study.moyak.chat.dto.request.UpdateTitleDTO;
import study.moyak.chat.entity.Chat;
import study.moyak.chat.repository.ChatRepository;
import study.moyak.chat.repository.MessageRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;


    @Transactional
    public ResponseEntity<?> createChat(MultipartFile allImage) throws IOException {
        Chat chat = new Chat();
        chat.setAll_image(allImage.getOriginalFilename());
        //chat.setRoom_name(String.valueOf(chat.getCreateDate())); // 처음 채팅방 생성됐을 때는 생성된 날짜로

        if(allImage.isEmpty()){
            return ResponseEntity.status(404).body("no Image");
        }else{
            //이미지 Base64 인코딩
            String base64Data = Base64.getEncoder().encodeToString(allImage.getBytes());
            chat.setAll_image(base64Data);

            chatRepository.save(chat);
            chat.setTitle(String.valueOf(chat.getCreateDate())); // 처음 채팅방 생성됐을 때는 생성된 날짜로

            return ResponseEntity.ok(chat.getId());
        }

    }

    @Transactional
    public ResponseEntity<?> getChat(Long chat_id) throws IOException {
        Chat chat = chatRepository.findById(chat_id)
                .orElseThrow(() -> new FileNotFoundException("채팅방을 찾을 수 없습니다."));

        List<EachPillDTO> eachPills = chat.getEachPills().stream()
                .map(pill -> new EachPillDTO(pill.getImage(), pill.getPill_name(), pill.getPill_ingredient()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ChatDTO(eachPills));
    }

    @Transactional
    public ResponseEntity<?> deleteChat(Long chat_id) throws IOException {
        Chat chat = chatRepository.findById(chat_id).orElseThrow(
                () -> new FileNotFoundException("채팅방을 찾을 수 없습니다."));

        chatRepository.delete(chat);

        return ResponseEntity.ok().body("채팅방 삭제 완료");
    }

    @Transactional
    public ResponseEntity<?> updateTitle(Long chat_id, String title) throws IOException {
        // 채팅방 조회, 없으면 예외 처리
        Chat chat = chatRepository.findById(chat_id).orElseThrow(
                () -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 채팅방 제목 수정
        chat.setTitle(title);

        // 변경 사항 저장
        chatRepository.save(chat);

        return ResponseEntity.ok().body("채팅방 제목 수정 완료");
    }
}
