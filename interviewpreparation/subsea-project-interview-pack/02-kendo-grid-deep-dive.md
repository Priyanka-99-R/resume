# 2. Kendo UI Grid — Deep Dive (THE signature file)

**This is your strongest story.** You moved Subsea's Kendo Grid from **client-side** to **server-side** data operations and **won a client-vs-server-side debate by demonstrating the lag**. If you nail one thing in the interview, make it this — it shows real engineering judgment, measurement over opinion, and full-stack ownership (Angular *and* .NET).

**Stack context:** Angular frontend using **Kendo UI for Angular** (the Grid), backend is **.NET (C#) Web API**, data in SQL. The grids displayed large schedule/equipment datasets with sorting, filtering, grouping, and inline editing.

---

## Q1. What is the Kendo Grid and why was it the right choice for Subsea?

**Answer.** Kendo UI is a commercial component library; the **Grid** is its flagship — a data table with built-in **paging, sorting, filtering, grouping, virtual scrolling, and inline editing**. Subsea needed to show large marine-equipment and schedule datasets with all of those operations, and building that from scratch in a plain `*ngFor` table would have been a huge, buggy effort. Kendo gave us those capabilities out of the box and, crucially, a **first-class pattern for pushing data operations to the server** — which became the centerpiece of my performance work.

**In Subsea.** The Schedule-Manager grids were the heart of my module — equipment and operation schedules with lots of rows and columns, and users constantly sorting/filtering/grouping them.

---

## Q2. Client-side vs server-side data binding — explain the difference. *(the core question)*

**Answer.** It's about **where the paging/sorting/filtering happens**:

- **Client-side (local data):** the browser loads the **entire dataset** once, and Kendo does paging/sort/filter **in JavaScript, in memory**. Fine for a few hundred rows. As the dataset grows it falls apart: a big initial payload over the wire, high browser memory, and every sort/filter/group churns the whole array on the main thread → visible lag and jank.

- **Server-side (remote data operations):** the grid sends the **current state** (page number, page size, sort descriptors, filter descriptors) to the backend; the **.NET API translates that into a SQL query** and returns **just the one page** of rows plus the **total count**. The browser only ever holds what's on screen. This scales to arbitrarily large datasets.

| | Client-side | Server-side |
|---|---|---|
| Data loaded | **entire dataset** up front | **one page** at a time |
| Where sort/filter runs | browser (JS, in memory) | **SQL in the .NET API** |
| Payload size | huge, grows with data | small, constant |
| Browser memory | high | low |
| Scales to large data? | **No** | **Yes** |
| Best for | small, static lists | large/growing datasets (Subsea) |

**In Subsea.** The grids started **client-side** and got sluggish as rows grew. I moved them to **server-side** so only one page ever crossed the wire — and that's the change I had to defend in the client-vs-server debate.

---

## Q3. Walk me through the exact server-side pattern you implemented. *(show the code)*

**Answer.** Kendo for Angular gives helpers that serialize the grid's `State` (page/sort/filter/group) into a query the .NET side understands, and the .NET Kendo extensions turn a `DataSourceRequest` straight into a paged SQL query. The two sides fit together like a contract.

**Angular side** — serialize the grid state and call the API:

```typescript
import { toDataSourceRequestString, translateDataSourceResultGroups }
  from '@progress/kendo-data-query';
import { State } from '@progress/kendo-data-query';
import { GridDataResult } from '@progress/kendo-angular-grid';

@Injectable({ providedIn: 'root' })
export class ScheduleGridService {
  constructor(private http: HttpClient) {}

  // state carries { skip, take, sort, filter, group }
  fetch(state: State): Observable<GridDataResult> {
    const query = toDataSourceRequestString(state);   // -> querystring the .NET side reads
    return this.http.get<{ data: any[]; total: number }>(`/api/schedule?${query}`)
      .pipe(map(res => ({ data: res.data, total: res.total } as GridDataResult)));
  }
}
```

**Component** — bind the grid and re-fetch on every state change:

```typescript
export class ScheduleGridComponent implements OnInit {
  public gridState: State = { skip: 0, take: 20, sort: [], filter: undefined };
  public gridData: GridDataResult;

  ngOnInit(): void { this.load(); }

  // fires on paging, sorting, filtering, grouping
  public onStateChange(state: State): void {
    this.gridState = state;
    this.load();
  }

  private load(): void {
    this.gridService.fetch(this.gridState)
      .pipe(takeUntil(this.destroy$))
      .subscribe(result => this.gridData = result);
  }
}
```

```html
<kendo-grid [data]="gridData" [pageSize]="gridState.take" [skip]="gridState.skip"
            [sort]="gridState.sort" [filter]="gridState.filter"
            [pageable]="true" [sortable]="true" [filterable]="true" [groupable]="true"
            (dataStateChange)="onStateChange($event)">
  <kendo-grid-column field="assetName" title="Asset"></kendo-grid-column>
  <kendo-grid-column field="scheduledDate" title="Date" filter="date"></kendo-grid-column>
  <kendo-grid-column field="status" title="Status"></kendo-grid-column>
</kendo-grid>
```

**.NET side** — let Kendo's server extensions do paging/sort/filter in the query:

```csharp
[ApiController]
[Route("api/schedule")]
public class ScheduleController : ControllerBase
{
    private readonly IScheduleService _service;
    public ScheduleController(IScheduleService service) => _service = service;

    // [DataSourceRequest] binds the querystring (page/sort/filter/group) into the request object
    [HttpGet]
    public DataSourceResult Get([DataSourceRequest] DataSourceRequest request)
    {
        IQueryable<Schedule> query = _service.QuerySchedules();   // IQueryable — NOT yet executed
        return query.ToDataSourceResult(request);   // translates to SQL: WHERE + ORDER BY + paging
    }
}
```

**The key insight:** `ToDataSourceResult(request)` is applied to an **`IQueryable`**, so the paging/sort/filter is composed into the **SQL query** and executed in the database — only the requested page of rows is materialized and returned, wrapped as `DataSourceResult { Data, Total }`. The `Total` drives the grid's pager.

**In Subsea.** This is exactly the pattern I built for Schedule-Manager. The `State` from the Kendo grid → `toDataSourceRequestString` → querystring → `[DataSourceRequest]` on the controller → `ToDataSourceResult` over `IQueryable` → one page of SQL rows back. That round-trip is what kept the grid responsive on large data.

---

## Q4. Why exactly was client-side slow, and how did you *prove* it? *(the debate story)*

**Answer.** Client-side was slow for three compounding reasons on a large dataset:
1. **Initial payload** — the API had to serialize and ship *every* row before the grid rendered anything.
2. **Browser memory** — the whole array sat in memory.
3. **Main-thread work** — every sort/filter/group re-processed the entire array in JavaScript, blocking the UI so the grid felt laggy and unresponsive.

There was a genuine disagreement on the team about whether server-side was worth the effort. Rather than argue in the abstract, I **demonstrated it**: I loaded the grid with a **realistic data volume** and let people *see* the lag on client-side sort/filter, then showed the server-side version staying snappy on the same data. Seeing the difference ended the debate — we agreed on server-side, and I committed to it fully.

**In Subsea.** That's my proudest engineering-judgment moment on this project: I didn't win on opinion, I won on **evidence and user impact**. And once the decision was made I owned it end to end — the Angular wiring and the .NET `DataSourceRequest` handling.

**The lesson I'll state:** *"I focus on the user impact and let a measurement settle the argument. I'll advocate hard for the right approach, but once the team decides, I commit — even if it isn't the one I proposed."*

---

## Q5. What is `DataSourceRequest` / `DataSourceResult`?

**Answer.**
- **`DataSourceRequest`** (server side, `[DataSourceRequest]` model binder) — the deserialized grid state: which page (`skip`/`take`... expressed as page + pageSize), the **sort descriptors**, the **filter descriptors**, and **group descriptors**. It's the request contract the grid sends.
- **`DataSourceResult`** — the response contract: `Data` (the current page of rows) + `Total` (the full unpaged count so the grid can render the pager) + aggregates/groups if requested.
- **`ToDataSourceResult(request)`** — the extension method that takes your `IQueryable` and applies filter → sort → paging as a composed query, executes it, and packages the `DataSourceResult`.

**In Subsea.** I relied on that contract so I didn't hand-write paging/sorting SQL — the Kendo server extensions translated the grid state into the query, and I returned the `DataSourceResult` straight from the controller.

---

## Q6. Paging, sorting, filtering, grouping — how do they each work server-side?

**Answer.** With the server-side pattern, all four are just descriptors in the `DataSourceRequest`, and `ToDataSourceResult` maps each to SQL:
- **Paging** → `skip`/`take` → SQL `OFFSET/FETCH` (only that page is materialized).
- **Sorting** → sort descriptors → SQL `ORDER BY`.
- **Filtering** → filter descriptors (per-column, with operators like `contains`, `eq`, date ranges) → SQL `WHERE`.
- **Grouping** → group descriptors → grouped aggregation; Kendo can request server grouping and lazy-load group contents.

The grid raises **one** `dataStateChange` event whenever any of these change; I re-send the whole state and get the correct page back — I don't handle each operation separately.

**In Subsea.** Users sorted by date/status, filtered equipment by category, and grouped operations — all of it resolved in the .NET API against SQL, so the browser never did the heavy lifting.

---

## Q7. What is virtual scrolling and did you use it?

**Answer.** **Virtual scrolling** renders only the rows currently in the viewport (plus a small buffer) into the DOM instead of all rows, and swaps them as you scroll. It keeps the DOM small even for large pages, which keeps scrolling smooth. With Kendo it pairs naturally with server-side data — the grid requests the next slice as you scroll.

**In Subsea.** On top of server-side data operations, I used **virtual scrolling** and made sure we **only requested the columns actually displayed**. The combination — server-side paging/sort/filter + virtual scrolling + lean columns — is what kept the grid responsive even on large schedule/equipment datasets.

---

## Q8. How did inline editing / updates work with the grid?

**Answer.** Kendo's grid supports inline/cell editing that emits edit events; on save I sent the changed row to a .NET `[HttpPut]`/`[HttpPost]` endpoint, and on success re-loaded the current page so the grid reflected server truth (including any server-side recalculation). Keeping the grid bound to server state avoided the client and server drifting apart.

**In Subsea.** A common production bug class here was **grid/state defects** — filters or edits not persisting correctly after a reload. Because the grid was bound to **server-side state**, the fix was usually making sure the state object (page/sort/filter) was preserved across the edit-and-reload cycle rather than silently reset. (More on that methodology in `05-behavioral-and-stars.md`.)

---

## Q9. Any downsides to server-side, and how did you handle them?

**Answer.** Honestly, yes:
- **More network round-trips** — every sort/filter/page is an API call. I mitigated with **`debounceTime`** on filter inputs and **`switchMap`** to cancel stale requests (so rapid filtering doesn't stack calls or cause race conditions — see `01-frontend.md`).
- **Backend has to be efficient** — the SQL needs proper **indexing** on sorted/filtered columns, or you just move the slowness to the DB. I made sure the columns we sort/filter on were indexed.
- **Slightly more complex** than "load it all" — but that complexity is the price of scaling, and I'd argue for it from the start next time.

**In Subsea.** One thing I'd standardize earlier: we had a **mix of client- and server-bound grids** across the app. Unifying on the server-side pattern from day one would have prevented the performance issues entirely — that's my honest "what I'd do differently."

---

### Rapid-fire recap

| Topic | Subsea answer |
|-------|---------------|
| Grid used for | large schedule/equipment datasets with sort/filter/group/edit |
| Client vs server | moved from client-side to **server-side** data operations |
| The pattern | Kendo `State` → `toDataSourceRequestString` → `[DataSourceRequest]` → `ToDataSourceResult(IQueryable)` → `DataSourceResult { Data, Total }` |
| Why server-side | one page over the wire vs whole dataset in browser memory |
| How I won the debate | **demonstrated the lag** on realistic data volume — evidence, not opinion |
| Paging/sort/filter/group | all become SQL `OFFSET/FETCH` / `ORDER BY` / `WHERE` / grouping |
| Virtual scrolling | yes — only render viewport rows; plus request only displayed columns |
| Downsides handled | debounce + `switchMap` for round-trips; indexed the sort/filter columns |
| What I'd change | unify all grids on server-side from the start |
