package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.*;

public final class GameBoardLayout {

    // ── 보드 스펙 상수 ─────────────────────────────────────────────
    private static final int SECTION_COUNT     = 6;
    private static final int SLOTS_PER_SECTION = 4;


    // ── 불변 상태 ─────────────────────────────────────────────────
    private final List<SectionLayout> sections;                 // 전체 섹션 (순서 유지, 불변)
    private final Map<Integer, SectionLayout> sectionById;      // id -> 섹션 (불변)
    private final Map<Integer, List<BoardSlot>> slotsById; // sectionId -> 슬롯들 (불변)
    private final List<BoardSlot> slots;                        // 전체 슬롯 (불변)
    private final Set<BoardSlot> slotSet;                       // 존재성 체크(O(1))

    // ── 생성 ─────────────────────────────────────────────────────
    public GameBoardLayout() {
        this.sections = buildSections();
        validateSections(this.sections);

        // 섹션 맵 (순서 보존)
        Map<Integer, SectionLayout> byId = new LinkedHashMap<>();
        for (SectionLayout section : sections) {
            byId.put(section.getSectionId(), section);
        }
        this.sectionById = Collections.unmodifiableMap(byId);

        Map<Integer, List<BoardSlot>> slotsById = buildSlotsById(this.sections);
        this.slotsById = Collections.unmodifiableMap(slotsById);
        this.slots = Collections.unmodifiableList(flattenSlots(slotsById, SECTION_COUNT * SLOTS_PER_SECTION));
        this.slotSet = Set.copyOf(this.slots);
    }

    private static Map<Integer, List<BoardSlot>> buildSlotsById(List<SectionLayout> sections) {
        Map<Integer, List<BoardSlot>> slotsBySection = new LinkedHashMap<>();
        for (SectionLayout section : sections) {
            List<TileType> types = section.getSlotTypes();
            List<BoardSlot> sectionSlots = new ArrayList<>(types.size());
            for (int i = 0; i < types.size(); i++) {
                sectionSlots.add(new BoardSlot(section.getSectionId(), i));
            }
            slotsBySection.put(section.getSectionId(), Collections.unmodifiableList(sectionSlots));
        }
        return slotsBySection;
    }

    private static List<BoardSlot> flattenSlots(Map<Integer, List<BoardSlot>> slotsBySection, int capacity) {
        List<BoardSlot> all = new ArrayList<>(capacity);
        for (List<BoardSlot> sectionSlots : slotsBySection.values()) {
            all.addAll(sectionSlots);
        }
        return all;
    }

    // ── 빌드/검증 ─────────────────────────────────────────────────
    private static List<SectionLayout> buildSections() {
        return List.of(
                new SectionLayout(1, List.of(TileType.BUILDING,  TileType.SHIP,     TileType.KNOWLEDGE, TileType.ANIMAL)),
                new SectionLayout(2, List.of(TileType.KNOWLEDGE, TileType.CASTLE,   TileType.BUILDING,  TileType.BUILDING)),
                new SectionLayout(3, List.of(TileType.ANIMAL,    TileType.BUILDING, TileType.SHIP,      TileType.KNOWLEDGE)),
                new SectionLayout(4, List.of(TileType.SHIP,      TileType.BUILDING, TileType.ANIMAL,    TileType.MINE)),
                new SectionLayout(5, List.of(TileType.MINE,      TileType.KNOWLEDGE,TileType.BUILDING,  TileType.BUILDING)),
                new SectionLayout(6, List.of(TileType.BUILDING,  TileType.ANIMAL,   TileType.CASTLE,    TileType.SHIP))
        );
    }

    private static void validateSections(List<SectionLayout> sections) {
        if (sections.size() != SECTION_COUNT) {
            throw new IllegalStateException("섹션 수가 올바르지 않습니다: " + sections.size());
        }
        for (SectionLayout s : sections) {
            List<TileType> types = s.getSlotTypes();
            if (types.size() != SLOTS_PER_SECTION) {
                throw new IllegalStateException("섹션 " + s.getSectionId() + "의 슬롯 수가 올바르지 않습니다: " + types.size());
            }
        }
    }

    // ── 조회 API ──────────────────────────────────────────────────
    /** 전체 슬롯(불변) */
    public List<BoardSlot> asSlots() {
        return slots;
    }

    /** 전체 섹션(불변) */
    public List<SectionLayout> getSections() {
        return sections;
    }

    /** 섹션 id → 섹션 (없으면 예외) */
    public SectionLayout getSection(int sectionId) {
        SectionLayout s = sectionById.get(sectionId);
        if (s == null) {
            throw new IllegalArgumentException("존재하지 않는 섹션: " + sectionId);
        }
        return s;
    }

    /** 섹션 id, 인덱스 → 슬롯 (없으면 예외) */
    public BoardSlot getSlot(int sectionId, int index) {
        List<BoardSlot> sec = slotsById.get(sectionId);
        if (sec == null) {
            throw new IllegalArgumentException("존재하지 않는 섹션: " + sectionId);
        }
        if (index < 0 || index >= sec.size()) {
            throw new IllegalArgumentException("섹션 " + sectionId + " 의 인덱스 범위 초과: " + index);
        }
        return sec.get(index);
    }

    /** 섹션 id → 슬롯 리스트(불변) */
    public List<BoardSlot> getSectionSlots(int sectionId) {
        List<BoardSlot> sec = slotsById.get(sectionId);
        if (sec == null) {
            throw new IllegalArgumentException("존재하지 않는 섹션: " + sectionId);
        }
        return sec;
    }

    /** 레이아웃 상 존재하는 슬롯인가? (O(1)) */
    public boolean contains(BoardSlot slot) {
        return slotSet.contains(slot);
    }

    /** 해당 슬룻이 허용하는 타입은 무엇인가? (O(1)) */
    public TileType getAllowedType(BoardSlot slot) {
        SectionLayout sec = getSection(slot.getSectionId());
        int slotIndex = slot.getIndex();
        List<TileType> types = sec.getSlotTypes();
        if (slotIndex < 0 || slotIndex >= types.size()) {
            throw new IllegalArgumentException("섹션 " + slot.getSectionId() + " 의 인덱스 범위 초과: " + slotIndex);
        }
        return types.get(slotIndex);
    }
}
